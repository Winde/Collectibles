package web.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import model.connection.ProductInfoConnector;
import model.connection.ProductInfoConnectorFactory;
import model.dataobjects.Category;
import model.dataobjects.CategoryValue;
import model.dataobjects.HierarchyNode;
import model.dataobjects.Image;
import model.dataobjects.Product;
import model.dataobjects.User;
import model.dataobjects.serializable.SerializableProduct;
import model.persistence.AuthorRepository;
import model.persistence.CategoryValueRepository;
import model.persistence.HierarchyRepository;
import model.persistence.ImageRepository;
import model.persistence.ProductRepository;
import model.persistence.UserRepository;

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
	private ProductInfoConnectorFactory connectorFactory;
	
	
	
	@RequestMapping(value="/product/find/{id}")
	public SerializableProduct product(@PathVariable String id) throws CollectiblesException {
		Long idLong = this.getId(id);
		
		if (idLong==null){
			throw new IncorrectParameterException(new String[]{"id"});
		} else {
			Product product = productRepository.findOne(idLong);
			if (product==null){
				throw new NotFoundException();
			}
			
			Map<String, ProductInfoConnector> connectors = connectorFactory.getConnectors();
			if (connectors!=null) {
				logger.info("Connectors: " + connectors);
				Iterator<Entry<String, ProductInfoConnector>> iterator = connectors.entrySet().iterator();
				while (iterator.hasNext()){
					Entry<String, ProductInfoConnector> entry = iterator.next();
					logger.info("Is "+entry.getKey()+" Processed? "+ (!(product.getProcessedConnectors()==null || !product.getProcessedConnectors().contains(entry.getKey()))));
					if (product.getProcessedConnectors()==null || !product.getProcessedConnectors().contains(entry.getKey())){
						ProductInfoConnector connector = entry.getValue();
						if (connector!=null){
							try {
								connector.updateProductTransaction(product);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				}				
			}
			
			
			
			return SerializableProduct.cloneProduct(product);			
		}
	}
	
	@Secured(value = { "ROLE_ADMIN" })
	@RequestMapping(value="/product/create/from/file/{hierarchy}", method = RequestMethod.POST)
	public Collection<SerializableProduct> addProductsFromFile(
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
							if (reference!=null && !"".equals(reference.trim())){ product.setReference(reference); }
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
								
						
						Map<String, ProductInfoConnector> connectors = connectorFactory.getConnectors();
						logger.info("Connectors: " + connectors);
						if (connectors!=null) {
							Iterator<Entry<String, ProductInfoConnector>> iterator = connectors.entrySet().iterator();
							while (iterator.hasNext()){
								Entry<String, ProductInfoConnector> entry = iterator.next();
								ProductInfoConnector connector = entry.getValue();
								if (connector!=null){
									connector.processInBackground(products);
								}
							}				
						}
						
						return SerializableProduct.cloneProduct(products);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
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
				productRepository.save(product);
				return SerializableProduct.cloneProduct(product);
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

		return SerializableProduct.cloneProduct(product);
	}
	
	@Secured(value = { "ROLE_ADMIN" })
	@RequestMapping(value="/product/modify/", method = RequestMethod.PUT)
	public SerializableProduct modifyProduct(@RequestBody SerializableProduct serializableProduct) throws CollectiblesException {		
		Product product = null;
		if (serializableProduct !=null) { product = serializableProduct.deserializeProduct(); }
		
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
				
				if (product.getUniversalReference()!=null && !product.getUniversalReference().equals(productInDb.getUniversalReference())){
					if (product.getProcessedConnectors()!=null){
						product.getProcessedConnectors().clear();
					}
				}
				product.setOwners(productInDb.getOwners());
				product.setAuthors(productInDb.getAuthors());								
				product.setCategoryValues(productInDb.getCategoryValues());
				product.setImages(productInDb.getImages());
				
				if (serializableProduct.getOwned()!=null){
					Authentication auth = SecurityContextHolder.getContext().getAuthentication();
					if (auth!=null){
						User user = new User();
						user.setUsername(auth.getName());
						if (!serializableProduct.getOwned()) {
							product.getOwners().remove(user);
						} else {
							user = userRepository.findOne(user.getUsername());
							product.getOwners().add(user);
						}
					}
				}
				
				Product result = productRepository.save(product);
				
				return SerializableProduct.cloneProduct(result);
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
						e.printStackTrace();
						throw new IncorrectParameterException(new String[]{"images[" + j + "]"});				
					}
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
	@RequestMapping(value="/product/reprocess/", method = RequestMethod.POST)
	public Boolean reProcess(){
		
		List<Product> products = productRepository.findAll();
		Map<String, ProductInfoConnector> connectors = connectorFactory.getConnectors();
		logger.info("Connectors: " + connectors);
		if (connectors!=null) {
			Iterator<Entry<String, ProductInfoConnector>> iterator = connectors.entrySet().iterator();
			while (iterator.hasNext()){
				Entry<String, ProductInfoConnector> entry = iterator.next();
				ProductInfoConnector connector = entry.getValue();
				if (connector!=null){
					connector.processInBackground(products);
				}
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
