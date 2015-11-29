package web.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;

import model.connection.ProductInfoConnector;
import model.connection.ProductInfoConnectorFactory;
import model.connection.ScrapeRequestorService;
import model.dataobjects.Category;
import model.dataobjects.CategoryValue;
import model.dataobjects.HierarchyNode;
import model.dataobjects.Image;
import model.dataobjects.Product;
import model.dataobjects.User;
import model.dataobjects.inmemory.ScrapeRequest;
import model.dataobjects.serializable.ConnectorInfo;
import model.dataobjects.serializable.SerializableProduct;
import model.persistence.AuthorRepository;
import model.persistence.CategoryValueRepository;
import model.persistence.HierarchyRepository;
import model.persistence.ImageRepository;
import model.persistence.ProductRepository;
import model.persistence.UserRepository;
import model.persistence.queues.ScrapeRequestRepository;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import web.supporting.error.exceptions.CollectiblesException;
import web.supporting.error.exceptions.IncorrectParameterException;
import web.supporting.error.exceptions.NotEnoughTimeToParseException;
import web.supporting.error.exceptions.NotFoundException;


@RestController
public class ProductController  extends CollectiblesController{

	private static final Logger logger = LoggerFactory.getLogger(ProductController.class);
	
	@Autowired
	private ProductRepository productRepository;

	@Autowired
	private ImageRepository imageRepository;

	@Autowired
	private AuthorRepository authorRepository;
	
	@Autowired
	private CategoryValueRepository categoryValueRepository;
	
	@Autowired
	private HierarchyRepository hierarchyRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ScrapeRequestRepository scrapeRequestRepository;

	@Autowired
	private EntityManager entityManager;
	
	@Autowired
	private ProductInfoConnectorFactory connectorFactory;
	
	@Autowired
	private ScrapeRequestorService scrapeRequestorService;
	
	
	private static final int MAX_PARSE_CHECKS = 20;//6;
	private static final int MILISECONDS_BETWEEN_PARSE_CHECKS = 900; 
	
	private Product checkProduct(Product product, List<ScrapeRequest> requests) throws NotEnoughTimeToParseException{
		int checks = 0;
		boolean pending = true;
		long startTime = new Date().getTime();
		while(checks<MAX_PARSE_CHECKS && requests != null && requests.size()>0 && pending){
			
			pending = scrapeRequestRepository.checkPending(requests);			
			try {
				Thread.sleep(MILISECONDS_BETWEEN_PARSE_CHECKS);
			} catch (InterruptedException e) {
				logger.error("Interrupted", e);
			}
			checks++; 
		}
		
		logger.info("Out of the loop in "+checks +" checks");		
		logger.info("Stopped WAITING!!" + (new Date().getTime()-startTime));
		
		//entityManager.detach(product);
		Product result = productRepository.findOneRefreshed(product.getId());
		
		if (checks>=MAX_PARSE_CHECKS){
			SerializableProduct serializableProduct = SerializableProduct.cloneProduct(result,ConnectorInfo.createConnectorInfo(connectorFactory.getConnectors(result)));
			throw new NotEnoughTimeToParseException(serializableProduct);
			
		} else {
			return result;
		}
		
	}
	
	@RequestMapping(value="/product/find/{id}", method = RequestMethod.GET)
	public SerializableProduct product(@PathVariable String id) throws CollectiblesException {
		Long idLong = this.getId(id);
		
		if (idLong==null){
			throw new IncorrectParameterException(new String[]{"id"});
		} else {
			Product product = productRepository.findOne(idLong);
			if (product==null){
				throw new NotFoundException();
			}
			
			return SerializableProduct.cloneProduct(product,ConnectorInfo.createConnectorInfo(connectorFactory.getConnectors(product)));			
		}
	}
	
	@Secured(value = { "ROLE_ADMIN" })
	@RequestMapping(value="/product/refresh/{id}", method = RequestMethod.PUT)
	public SerializableProduct productRefresh(@PathVariable String id) throws CollectiblesException, NotEnoughTimeToParseException {
		Long idLong = this.getId(id);
		
		if (idLong==null){
			throw new IncorrectParameterException(new String[]{"id"});
		} else {
			Product product = productRepository.findOne(idLong);
			if (product==null){
				throw new NotFoundException();
			}
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			if (auth!=null){
				List<ScrapeRequest> requestIds =scrapeRequestorService.scrapeProduct(auth.getName(),product,true,false);
				product = checkProduct(product, requestIds);
			}
			return SerializableProduct.cloneProduct(product,ConnectorInfo.createConnectorInfo(connectorFactory.getConnectors(product)));			
		}
	}
	
	@Secured(value = { "ROLE_ADMIN" })
	@RequestMapping(value="/product/refresh/", method = RequestMethod.PUT)
	public SerializableProduct productSaveAndRefresh(@RequestBody SerializableProduct product) throws CollectiblesException, NotEnoughTimeToParseException {
		SerializableProduct result = this.modifyProductScrapeOption(product,true);
							
		return result;
	}
	
	
	@Secured(value = { "ROLE_ADMIN" })
	@RequestMapping(value="/product/create/from/file/{hierarchy}", method = RequestMethod.POST)
	public Boolean addProductsFromFile(
			@PathVariable String hierarchy,
			@RequestPart("file") MultipartFile file) throws CollectiblesException {
		Long hierarchyId = this.getId(hierarchy);
			
		if (hierarchyId!=null){
			
			HierarchyNode hierarchyNode = hierarchyRepository.findOne(hierarchyId);
			if (hierarchyNode!=null){
				if (file!=null){
					Collection<Product> products = new ArrayList<>();					
					InputStream inputStream = null;
					try {
						inputStream = file.getInputStream();					
						InputStreamReader reader = new InputStreamReader(inputStream);					
						CSVParser parser = CSVFormat.EXCEL.withHeader().parse(reader);
						
						List<SimpleDateFormat> formats = new ArrayList<>();
						formats.add(new SimpleDateFormat("yyyy-mm-dd"));
						formats.add(new SimpleDateFormat("yyyy"));
						
						
						for (CSVRecord record : parser.getRecords()){
							String name = record.get("name");
							String description = record.get("description");
							String owned = record.get("owned");
							String reference = record.get("reference");
							String date = record.get("date");
							String ISBN = record.get("ISBN");							
							Product product = new Product();
							if (name!=null && !"".equals(name.trim())){ product.setName(name); }
							if (description!=null && !"".equals(description.trim())){ product.setDescription(description); }
							//if (owned !=null && owned.trim().equals("true")){product.setOwned(true); }
							if (ISBN !=null && !"".equals(ISBN.trim())) { product.setUniversalReference(ISBN.replaceAll("-", "")); }
							if (date!=null && !date.trim().equals("")){
								for (SimpleDateFormat format : formats ){
									try {
										Date parsedDate = format.parse(date);
										if (parsedDate!=null){
											product.setReleaseDate(parsedDate);
										}
									} catch (ParseException e) {}
								}							
							}
							product.setHierarchyPlacement(hierarchyNode);							
							validate(product);
							
							products.add(product);
							
						}				
																		
						//productRepository.saveWithImages(products,images);
						productRepository.save(products);
						Authentication auth = SecurityContextHolder.getContext().getAuthentication();
						if (auth!=null){
							String username = null;						
							username = auth.getName();
							scrapeRequestorService.request(products, username, false,false);							
						}
						
						
						return Boolean.TRUE;
					} catch (IOException e) {
						logger.error("Exception when scraping uploading CSV", e);
						throw new IncorrectParameterException(new String[]{"file"});
					}
					
				} else {
					throw new IncorrectParameterException(new String[]{"file"});
				}				
			} else {
				throw new NotFoundException(new String[]{"hierarchy"});
			}
		} else {
			throw new IncorrectParameterException(new String[]{"hierarchy"});
		}		
	}
	
	@Secured(value = { "ROLE_ADMIN" })
	@RequestMapping(value="/product/create/{hierarchy}", method = RequestMethod.POST)
	public SerializableProduct addProduct(@RequestBody SerializableProduct serializableProduct,@PathVariable String hierarchy) throws CollectiblesException {
		Long hierarchyId = this.getId(hierarchy);
		Product product = null;
		if (serializableProduct !=null) { product = serializableProduct.deserializeProduct(); }
		
		if (hierarchyId!=null){
			
			HierarchyNode hierarchyNode = hierarchyRepository.findOne(hierarchyId);
			if (hierarchyNode!=null){
				product.setId(null);
				product.setHierarchyPlacement(hierarchyNode);
				this.validate(product);				
				Product result = productRepository.save(product);
				
				Authentication auth = SecurityContextHolder.getContext().getAuthentication();
				if (auth!=null){
					String username = auth.getName();
					scrapeRequestorService.scrapeProduct(username,result,true,false);
				}
				return SerializableProduct.cloneProduct(result,ConnectorInfo.createConnectorInfo(connectorFactory.getConnectors(result)));
			} else {
				throw new NotFoundException(new String[]{"hierarchy"});
			}
		} else {
			throw new IncorrectParameterException(new String[]{"hierarchy"});
		}		
		
	}
	
	@Secured(value = { "ROLE_ADMIN" })
	@RequestMapping(value="/product/remove/{id}", method = RequestMethod.POST)
	public Long removeProduct(@PathVariable String id) throws CollectiblesException {		
		Long idLong = this.getId(id);
		
		if (idLong==null){
			throw new IncorrectParameterException(new String[]{"id"});
		} else {		
			try {
				productRepository.delete(idLong);
			}catch(EmptyResultDataAccessException ex) {				
				throw new NotFoundException(new String[]{"product"});
			}
			return idLong;
		}
	}
	
	@Secured(value = { "ROLE_ADMIN" })
	@RequestMapping(value="/product/find/{productId}/category/value/add/{categoryValueId}", method = RequestMethod.POST)
	public SerializableProduct addCategoryValue(@PathVariable String productId, @PathVariable String categoryValueId) throws CollectiblesException {
		Long longProductId = this.getId(productId);
		Long longCategoryValueId = this.getId(categoryValueId);
		List<String> errors = new ArrayList<>();
		
		if (longProductId==null) { errors.add("product"); }
		if (longCategoryValueId==null) { errors.add("categoryValue"); }
		if (errors.size()>0){
			throw new IncorrectParameterException(errors);
		}
		
		CategoryValue categoryValue = categoryValueRepository.findOne(longCategoryValueId);
		Product product = productRepository.findOne(longProductId);		
		if (product==null){ errors.add("product");}
		if (categoryValue==null){ errors.add("categoryValue");}
		if (errors.size()>0){
			throw new NotFoundException(errors);
		}
		
		Category category = categoryValue.getCategory();
		Collection<Category> productCategories = product.getHierarchyPlacement().getCategories();

		if (productCategories == null || category == null || !productCategories.contains(category)){
			throw new IncorrectParameterException(new String[]{"category"});
		}
		
		product.addCategoryValue(categoryValue);
		productRepository.save(product);		

		return SerializableProduct.cloneProduct(product,ConnectorInfo.createConnectorInfo(connectorFactory.getConnectors(product)));
	}
	
	@Secured(value = { "ROLE_USER" })
	@RequestMapping(value="/product/update/price/{id}", method = RequestMethod.PUT)
	public SerializableProduct updatePrice(@PathVariable String id) throws CollectiblesException, NotEnoughTimeToParseException {		
		Long idLong = this.getId(id);
		
		if (idLong==null){
			throw new IncorrectParameterException(new String[]{"id"});
		} else {
			Product product = productRepository.findOne(idLong);
			if (product==null){
				throw new NotFoundException();
			}
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			if (auth!=null){
				String username = null;
				List<ScrapeRequest> requests =  scrapeRequestorService.scrapeProduct(username, product,true,true);
				product = checkProduct(product, requests);
			}
			return SerializableProduct.cloneProduct(product,ConnectorInfo.createConnectorInfo(connectorFactory.getConnectors(product)));			
		}			
	}
	
	@Secured(value = { "ROLE_ADMIN" })
	@RequestMapping(value="/product/modify/", method = RequestMethod.PUT)
	public SerializableProduct modifyProductDefault(@RequestBody SerializableProduct serializableProduct)throws CollectiblesException, NotEnoughTimeToParseException {
		return modifyProductScrapeOption(serializableProduct,false);
	}
	
	@Secured(value = { "ROLE_USER" })
	@RequestMapping(value="/product/modify/minor/", method = RequestMethod.PUT)
	public SerializableProduct modifyLiteProduct(@RequestBody SerializableProduct serializableProduct) throws CollectiblesException {
		Product product = null;
		if (serializableProduct !=null) { product = serializableProduct.deserializeProduct(); }
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		User user = null;
		if (auth!=null && auth.getName()!=null){
			user = userRepository.findOne(auth.getName());
		}
		
		if (user!=null){
			Product productInDb = productRepository.findOne(product.getId());
			
			HierarchyNode hierarchy = product.getHierarchyPlacement();
			if (hierarchy!=null && hierarchy.getId()!=null){
				HierarchyNode currentHierarchy = productInDb.getHierarchyPlacement();
				if (currentHierarchy == null || !hierarchy.equals(currentHierarchy)){
					hierarchy = hierarchyRepository.findOne(hierarchy.getId());
					if (hierarchy!=null){
						productInDb.setHierarchyPlacement(hierarchy);
					}
				}
			}
			
			
			if (productInDb!=null){			
				if (serializableProduct.getWished()!=null){
					if (user!=null){
						if (!serializableProduct.getWished()) {
							if (productInDb.getWishers()!=null){
								productInDb.getWishers().remove(user);
							}
						} else {
							Set<User> owners = productInDb.getWishers();
							if (owners==null){
								owners = new HashSet<>();
								productInDb.setWishers(owners);
							}						
							productInDb.getWishers().add(user);
						}
					}
				}
				
				if (serializableProduct.getOwnedAnotherLanguage()!=null){
					
					if (user!=null){
						if (!serializableProduct.getOwnedAnotherLanguage()) {
							if (productInDb.getOwnersOtherLanguage()!=null){
								productInDb.getOwnersOtherLanguage().remove(user);
							}
						} else {
							Set<User> owners = productInDb.getOwnersOtherLanguage();
							if (owners==null){
								owners = new HashSet<>();
								productInDb.setOwnersOtherLanguage(owners);
							}						
							productInDb.getOwnersOtherLanguage().add(user);
						}
					}
				}
				
				if (serializableProduct.getOwned()!=null){
					if (user!=null){
						if (!serializableProduct.getOwned()) {
							if (productInDb.getOwners()!=null){
								productInDb.getOwners().remove(user);
							}
						} else {
							Set<User> owners = productInDb.getOwners();
							if (owners==null){
								owners = new HashSet<>();
								productInDb.setOwners(owners);
							}
							
							productInDb.getOwners().add(user);
						}
					}
				}
				Product result = productRepository.save(productInDb);
				
				return SerializableProduct.cloneProduct(result,ConnectorInfo.createConnectorInfo(connectorFactory.getConnectors(result)));
			} else {
				throw new NotFoundException(new String[]{"product"});
			}
		} else {
			throw new NotFoundException(new String[]{"user"});
		}
	} 
	
	public SerializableProduct modifyProductScrapeOption(SerializableProduct serializableProduct, boolean scrape) throws CollectiblesException, NotEnoughTimeToParseException {		
		Product product = null;
		if (serializableProduct !=null) { product = serializableProduct.deserializeProduct(); }
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		User user = null;
		if (auth!=null && auth.getName()!=null){
			user = userRepository.findOne(auth.getName());
		}
		this.validate(product);
		
		if (product.getId()!=null){
			Product productInDb = productRepository.findOne(product.getId());
						
			if (productInDb!=null){				
				if ( 
						product.getHierarchyPlacement()==null || 
						product.getHierarchyPlacement().getId()==null || 
						hierarchyRepository.findOne(product.getHierarchyPlacement().getId())==null
				){								
					product.setHierarchyPlacement(productInDb.getHierarchyPlacement());
				} 	
								
				product.setRatings(productInDb.getRatings());
				product.setPrices(productInDb.getPrices());
				product.setOwners(productInDb.getOwners());
				product.setOwnersOtherLanguage(productInDb.getOwnersOtherLanguage());
				product.setWishers(productInDb.getWishers());
				product.setAuthors(productInDb.getAuthors());								
				product.setCategoryValues(productInDb.getCategoryValues());
				product.setImages(productInDb.getImages());
				product.setProcessedConnectors(productInDb.getProcessedConnectors());
				//product.setConnectorReferences(productInDb.getConnectorReferences());

				if (serializableProduct.getWished()!=null){
					if (user!=null){
						if (!serializableProduct.getWished()) {
							if (product.getWishers()!=null){
								product.getWishers().remove(user);
							}
						} else {
							Set<User> owners = product.getWishers();
							if (owners==null){
								owners = new HashSet<>();
								product.setWishers(owners);
							}						
							product.getWishers().add(user);
						}
					}
				}
				
				if (serializableProduct.getOwnedAnotherLanguage()!=null){
					
					if (user!=null){
						if (!serializableProduct.getOwnedAnotherLanguage()) {
							if (product.getOwnersOtherLanguage()!=null){
								product.getOwnersOtherLanguage().remove(user);
							}
						} else {
							Set<User> owners = product.getOwnersOtherLanguage();
							if (owners==null){
								owners = new HashSet<>();
								product.setOwnersOtherLanguage(owners);
							}						
							product.getOwnersOtherLanguage().add(user);
						}
					}
				}
				
				if (serializableProduct.getOwned()!=null){
					if (user!=null){
						if (!serializableProduct.getOwned()) {
							if (product.getOwners()!=null){
								product.getOwners().remove(user);
							}
						} else {
							Set<User> owners = product.getOwners();
							if (owners==null){
								owners = new HashSet<>();
								product.setOwners(owners);
							}
							
							product.getOwners().add(user);
						}
					}
				}

				Product result = productRepository.save(product);
				
				if (scrape){					
					if (user!=null){
						String username = user.getUsername();
						List<ScrapeRequest> requests = scrapeRequestorService.scrapeProduct(username,result,true,false);
						result = checkProduct(result, requests);
					}
				}

				return SerializableProduct.cloneProduct(result,ConnectorInfo.createConnectorInfo(connectorFactory.getConnectors(result)));
			} else {
				throw new NotFoundException(new String[]{"product"});
			}
		} else {
			throw new IncorrectParameterException(new String[]{"product.id"});
		}			
	}
	
	@Secured(value = { "ROLE_ADMIN" })
	@RequestMapping(value="/product/{id}/image/add/", method = RequestMethod.POST)
	public List<Image> addImageToProduct(@PathVariable String id,			
            @RequestPart("images") MultipartFile[] files)
            throws CollectiblesException {
		
		List<String> errors = this.validateImages(files);
		
		if (errors!=null && errors.size()>0){
			throw new IncorrectParameterException(errors);
		}
		
		Long productId = this.getId(id);
		if (productId!=null){
			Product productInDb = productRepository.findOne(productId);
			if (productInDb!=null){		
				
				List<Image> images = new ArrayList<>();
				int j=0;
				for (MultipartFile file : files) {
					Image image = new Image();	
					try {
						image.setData(file.getBytes());
					} catch (IOException e) {
						logger.error("Exception reading image bytes", e);
						throw new IncorrectParameterException(new String[]{"images[" + j + "]"});				
					}
					image.setProduct(productInDb);
					images.add(image);
					j++;
				}
				
				productRepository.addImage(productInDb, images);
				return images;
			} else {
				throw new NotFoundException(new String[]{"product"});
			}			
		} else {
			throw new IncorrectParameterException(new String[]{"id"});
		}
	}
	
	@Secured(value = { "ROLE_ADMIN" })
	@RequestMapping(value="/product/{productId}/image/remove/{imageId}", method = RequestMethod.POST)
	public Long removeImageFromProduct(@PathVariable String productId,@PathVariable String imageId) throws CollectiblesException{
		Long longProductId = this.getId(productId);
		Long longImageId = this.getId(imageId);
		
		List<String> errors = new ArrayList<>();
		if (longProductId == null) { errors.add("product.id"); }
		if (longImageId == null) { errors.add("image.id"); }
		
		if (errors.size()>0){
			throw new IncorrectParameterException(errors);
		} else {			
			Product product = productRepository.findOne(longProductId);
			if (product ==null) {
				throw new NotFoundException(new String[]{"product"});
			} else{				
				try {
					boolean result = productRepository.removeImage(product,longImageId);
					if (result){
						return longImageId;
					} else {
						throw new NotFoundException(new String[]{"product.image"});
					}
				}catch(EmptyResultDataAccessException ex) {				
					throw new NotFoundException(new String[]{"image"});
				}
				
			}
			
		}
		
	}
	
	
	@Secured(value = { "ROLE_ADMIN" })
	@RequestMapping(value="/product/create/from/{connectorName}/user/{userId}/tohierarchy/{hierarchy}", method = RequestMethod.POST)
	public Integer reProcessAll(
			@PathVariable String userId,
			@PathVariable String connectorName,
			@PathVariable String hierarchy) throws CollectiblesException{
		Integer toBeProcessed = null;
		
		Long hierarchyId = super.getId(hierarchy);
		
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		User user = null;
		if (auth!=null){
			logger.info("Searching for user: " + auth.getPrincipal());
			user = userRepository.findOne((String) auth.getPrincipal());
			logger.info("Found user: " + user);
		}
		if (user!=null){
			if (hierarchyId!=null){
				logger.info("Searching for hierarchy: " + hierarchyId);
				HierarchyNode hierarchyNode = hierarchyRepository.findOne(hierarchyId);
				logger.info("Found hierarchy: " + hierarchyNode);
				if (hierarchyNode!=null){								
					ProductInfoConnector connector = connectorFactory.getConnector(connectorName);
					if (connector!=null && connector.supportsImportingProducts()){						
						try {
							String identifier = connector.getIdentifier();
							String userName = user.getUsername();
							List<String> references = connector.getMultipleReferences(userId);
							List<ScrapeRequest> scrapeRequests = new ArrayList<>();
							for (String reference : references){
								ScrapeRequest request = new ScrapeRequest();
								request.setConnector(identifier);
								request.setLiveRequest(false);
								request.setOnlyTransient(false);
								request.setProductId(null);
								request.setProductReference(reference);
								request.setRequestTime(new Date());
								request.setUserId(userName);
								request.setHierarchy(hierarchyNode.getId());
								scrapeRequests.add(request);			
							}
							scrapeRequestRepository.save(scrapeRequests);
							toBeProcessed = scrapeRequests.size();
						} catch (Exception e) {
							throw new NotFoundException(new String[]{"user"});
						}												
					} else {
						throw new IncorrectParameterException(new String[]{"connector"});
					}
				}else {
					throw new NotFoundException(new String[]{"hierarchy"});
				}
			} else {
				throw new IncorrectParameterException(new String[]{"hierarchy"});
			}
		} else {
			throw new NotFoundException(new String[]{"user"});
		}
				
		return toBeProcessed;
	}
	
	@Secured(value = { "ROLE_ADMIN" })
	@RequestMapping(value="/product/all/reprocess/", method = RequestMethod.POST)
	public Boolean reProcessAll(){
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth!=null){
			String username = auth.getName();
			logger.info("Querying all products in database");
			List<Product> products = productRepository.findAll();			
			scrapeRequestorService.request(products, username, false,false);			
		}
		return Boolean.TRUE;
	}
	
	@Secured(value = { "ROLE_ADMIN" })
	@RequestMapping(value="/product/all/update/prices/", method = RequestMethod.POST)
	public Boolean updatePricessAll(){
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth!=null){
			String username = auth.getName();
			List<Product> products = productRepository.findAll();			
			scrapeRequestorService.request(products, username, false,true);
		}
		return Boolean.TRUE;
	}
	
	@Secured(value = { "ROLE_ADMIN" })
	@RequestMapping(value="/product/all/update/thumb/", method = RequestMethod.POST)
	public Boolean updateThumbAll(){
		
		Iterable<Image> images = imageRepository.findAll();
		for (Image image: images){
			if (image.getData()!=null){
				image.createThumb(image.getData());
				imageRepository.save(image);
			}
		}
		

		return Boolean.TRUE;
	}
	
	private List<String> validateImages(MultipartFile[] files){
		List<String> errors = new ArrayList<>();
		if (files==null || files.length<=0){
			errors.add("images");
			return errors;										
		} else {								
			int i=0;
			for (MultipartFile file : files) {
				if (file.getContentType()==null || !file.getContentType().startsWith("image")){
					errors.add("images[" + i + "].content-type");					
				}
				i++;
			}
		}
		return errors;
	}
}
