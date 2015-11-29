package web.controllers;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import model.connection.ProductInfoConnector;
import model.connection.ProductInfoConnectorFactory;
import model.dataobjects.CategoryValue;
import model.dataobjects.HierarchyNode;
import model.dataobjects.Product;
import model.dataobjects.User;
import model.dataobjects.inmemory.ScrapeRequest;
import model.dataobjects.serializable.SerializableProduct;
import model.dataobjects.serializable.SerializableProduct.ProductComplexView;
import model.dataobjects.serializable.SerializableProduct.ProductListView;
import model.dataobjects.supporting.ObjectList;
import model.persistence.HierarchyRepository;
import model.persistence.ProductRepository;
import model.persistence.UserRepository;
import model.persistence.queryParameters.ProductSearch;
import model.persistence.queues.ScrapeRequestRepository;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import web.supporting.error.exceptions.CollectiblesException;
import web.supporting.error.exceptions.IncorrectParameterException;

import com.fasterxml.jackson.annotation.JsonView;

import edu.emory.mathcs.backport.java.util.Arrays;


@RestController
public class SearchController extends CollectiblesController{

	private static final Logger logger = LoggerFactory.getLogger(SearchController.class);
		
	@Autowired
	private ProductRepository productRepository;
	
	@Autowired
	private HierarchyRepository hierarchyRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ProductInfoConnectorFactory connectorFactory;

	@Autowired
	private ScrapeRequestRepository scrapeRequestRepository;

	
	private final String defaultPaginationSizeString = "50";
	private final int defaultPaginationSize = 50;
	private final int maxPaginationSize = 150;
	
	private ObjectList<SerializableProduct> findProduct(
			HierarchyNode hierarchyNode, 
			String search, 
			Collection<String> categoryValuesIds, 
			String withImagesString,
			String withPriceString,
			String ownedString,
			String ownedByString,
			String wishedByString,
			String store,
			String seller,
			String sortBy,
			String sortOrder,
			Integer page,
			Integer maxResults,
			boolean forcePagination) throws CollectiblesException {

			Boolean withImages = null;			
			Boolean withPrice = null;

			boolean errorReturnNoResults = false;
			ProductSearch searchObject = new ProductSearch();
			
			searchObject.setSearchTerm(search);			
			
			if (forcePagination){
				if (page==null ) {
					page = 0;
				}
				if (maxResults == 0 || maxResults == null){
					maxResults = defaultPaginationSize;
				} else if (maxResults > maxPaginationSize){
					maxResults = defaultPaginationSize;
				}				
			}
			
			searchObject.setPage(page);
			searchObject.setMaxResults(maxResults);
			

			Boolean owned = null;
			
			if (ownedString!=null && ownedString.equals("true")){
				owned = Boolean.TRUE;
			} else if (ownedString!=null && ownedString.equals("false")){
				owned = Boolean.FALSE;
			}
						
			if (owned!=null){
				Authentication auth = SecurityContextHolder.getContext().getAuthentication();				
				User owner = null;
				if (auth!=null){
					owner = userRepository.findOne(auth.getName());
				}
				
				if (owner!=null){
					if (owned){ 
						searchObject.addUsersWhoOwn(owner); 
					} else {
						searchObject.addUsersWhoDontOwn(owner); 
					}
				} else {
					logger.info("Skipping owned parameter due to user probably has been logged out");					
				}
			}
			
			if (ownedByString!=null && !ownedByString.trim().equals("")){
				Long userId = null;
				try {
					userId = Long.parseLong(ownedByString);
				}catch (Exception e){
					logger.error("Exception reading user id as long for search", e);
				}
				if (userId!=null){
					User user = userRepository.findById(userId);				
					if (user!=null){
						searchObject.addUsersWhoOwn(user);
						if (searchObject.getUsersWhoDontOwn()!=null && searchObject.getUsersWhoDontOwn().contains(user)){
							logger.info("Setting error due to search for who owns and doesnt own");
							errorReturnNoResults = true;
						}
					} else {
						logger.info("Setting error due to user not existing");
						errorReturnNoResults = true;
					}
				}
			}
			
			if (wishedByString!=null && !wishedByString.trim().equals("")){
				Long userId = null;
				try {
					userId = Long.parseLong(wishedByString);
				}catch (Exception e){
					logger.error("Exception reading user id as long for search", e);
				}
				if (userId!=null){
					User user = userRepository.findById(userId);				
					if (user!=null){
						searchObject.addWisher(user);
					} else {
						logger.info("Setting error due to user not existing");
						errorReturnNoResults = true;
					}
				}
			}
		
			
			if (withImagesString!=null && withImagesString.equals("true")){
				withImages = Boolean.TRUE;
			} else if (withImagesString!=null && withImagesString.equals("false")){
				withImages = Boolean.FALSE;
			}
								
			searchObject.setWithImages(withImages);
		

			if (withPriceString!=null && withPriceString.equals("true")){
				withPrice = Boolean.TRUE;
			} else if (withPriceString!=null && withPriceString.equals("false")){
				withPrice = Boolean.FALSE;
			}
			
			searchObject.setWithPrice(withPrice);
			
			searchObject.setStore(store);
			searchObject.setSeller(seller);
			
			
			searchObject.setHierarchy(hierarchyNode);
			
						
			List<CategoryValue> categoryValues = new ArrayList<>();
			if (categoryValuesIds!=null){
				for (String categoryValueString : categoryValuesIds){
					Long categoryId = this.getId(categoryValueString);
					if (categoryId==null){
						throw new IncorrectParameterException(new String[]{"categories"});
					} else {
						CategoryValue categoryValue = new CategoryValue();
						categoryValue.setId(categoryId);
						categoryValues.add(categoryValue);
					}
				}
			}
			
			searchObject.setCategoryValues(categoryValues);
			

			searchObject.setSortBy(sortBy);
			searchObject.setSortOrder(sortOrder);
			
			logger.info("Sort by: " + sortBy);
			logger.info("Sort Order: " + sortOrder);
			
			ObjectList<Product> resultFromDB = null;

			if (!errorReturnNoResults){
				Collection<String> errors = searchObject.errors();
				logger.info("Errors in search: " + errors );
				if (errors == null || errors.size()<=0){
					resultFromDB = productRepository.searchProduct(searchObject);
				}else {
					throw new IncorrectParameterException(errors);
				}
			} else {
				resultFromDB = new ObjectList<>();
				resultFromDB.setObjects(new ArrayList<>());
			}
			
			ObjectList<SerializableProduct> result = null;

			List<String> ignorePropertiesForSearch = new ArrayList<>();
			
			for(Field field  : SerializableProduct.class.getDeclaredFields())
			{
			    if (field.isAnnotationPresent(JsonView.class))
			        {
			    		JsonView annotation = field.getDeclaredAnnotation(JsonView.class);
			    		Class<?>[] views = annotation.value();
			    		List viewList = Arrays.asList(views);
			    		
			    		if (viewList!=null && viewList.contains(ProductComplexView.class)){
			    			ignorePropertiesForSearch.add(field.getName());
			    		}
			        }
			}
			
			
			String [] ignorePropertiesForSearchArray = ignorePropertiesForSearch.toArray(new String[ignorePropertiesForSearch.size()]);
			
			logger.info("Ignore: " + ignorePropertiesForSearch );

			if (resultFromDB!=null && resultFromDB.getObjects()!=null) {
				logger.info("Result objects number: " + resultFromDB.getObjects().size());
				result = new ObjectList<>();
				result.setHasNext(resultFromDB.getHasNext());
				result.setMaxResults(resultFromDB.getMaxResults());				
				result.setObjects(SerializableProduct.cloneProduct(resultFromDB.getObjects(),ignorePropertiesForSearchArray));				
			}
			
			return result;
	}
		
	@JsonView(model.dataobjects.serializable.SerializableProduct.ProductListView.class)	
	@RequestMapping(value="/product/search", method = RequestMethod.GET)
	public ObjectList<SerializableProduct> search(HttpServletRequest request, 			
			@RequestParam(required=false, name="withImages") String withImagesString,
			@RequestParam(required=false, name="search") String searchString,
			@RequestParam(required=false, name="owned" ) String owned,
			@RequestParam(required=false, name="ownedBy" ) String ownedBy,
			@RequestParam(required=false, name="wishedBy" ) String wishedBy,
			@RequestParam(required=false, name="withPrice") String withPrice,
			@RequestParam(required=false, name="store") String store,
			@RequestParam(required=false, name="seller") String seller,
			@RequestParam(required=false, name="sortBy") String sortBy,
			@RequestParam(required=false, name="sortOrder") String sortOrder,
			@RequestParam(required=true, name="page") int page,
			@RequestParam(required=false, name="maxResults", defaultValue=defaultPaginationSizeString) int maxResults) throws CollectiblesException{					
		ObjectList<SerializableProduct> objects = findProduct(null,searchString,null,withImagesString,withPrice,owned,ownedBy,wishedBy,store,seller,sortBy,sortOrder,page,maxResults,true);
		return objects;
	}
	
	@JsonView(ProductListView.class)
	@RequestMapping(value="/product/search/{hierarchy}/", method = RequestMethod.GET)
	public ObjectList<SerializableProduct> searchHierarchy(HttpServletRequest request, 
			@PathVariable String hierarchy, 
			@RequestParam(required=false, name="search") String searchString,
			@RequestParam(required=false, name="withImages") String withImagesString,
			@RequestParam(required=false, name="withPrice") String withPrice,
			@RequestParam(required=false, name="owned" ) String owned,
			@RequestParam(required=false, name="ownedBy" ) String ownedBy,
			@RequestParam(required=false, name="wishedBy" ) String wishedBy,
			@RequestParam(required=false, name="store") String store,
			@RequestParam(required=false, name="seller") String seller,
			@RequestParam(required=false, name="categoryValues" ) List<String> categories,
			@RequestParam(required=false, name="sortBy") String sortBy,
			@RequestParam(required=false, name="sortOrder") String sortOrder,
			@RequestParam(required=true, name="page") int page,
			@RequestParam(required=false, name="maxResults", defaultValue=defaultPaginationSizeString) int maxResults) throws CollectiblesException{					
		
		
		
		
		HierarchyNode node = null;
		if (hierarchy!=null && !"".equals(hierarchy.trim())){
			Long hierarchyId = this.getId(hierarchy);
			
			if (hierarchyId==null){				
				throw new IncorrectParameterException(new String[]{"hierarchy"});
			} else {								
				node = hierarchyRepository.findOne(hierarchyId);					
				if (node==null){
					throw new IncorrectParameterException(new String[]{"hierarchy"});
				}
			}
		}
		
		ObjectList<SerializableProduct> objects = findProduct(node,searchString,categories,withImagesString,withPrice,owned,ownedBy,wishedBy,store,seller,sortBy,sortOrder,page,maxResults,true);		
		return objects;	
	}
	
	
	@Secured(value = { "ROLE_ADMIN" })
	@RequestMapping(value="/product/update/prices/{hierarchy}/", method = RequestMethod.POST)
	public Boolean updatePricesHierarchy(HttpServletRequest request, HttpServletResponse response,
			@PathVariable String hierarchy, 
			@RequestParam(required=false, name="search") String searchString,
			@RequestParam(required=false, name="withImages") String withImagesString,
			@RequestParam(required=false, name="withPrice") String withPrice,
			@RequestParam(required=false, name="owned" ) String owned,
			@RequestParam(required=false, name="ownedBy" ) String ownedBy,
			@RequestParam(required=false, name="wishedBy" ) String wishedBy,			
			@RequestParam(required=false, name="store") String store,
			@RequestParam(required=false, name="seller") String seller,			
			@RequestParam(required=false, name="categoryValues" ) List<String> categories) throws CollectiblesException{						
		
		HierarchyNode node = null;
		if (hierarchy!=null && !"".equals(hierarchy.trim())){
			Long hierarchyId = this.getId(hierarchy);
			
			if (hierarchyId==null){				
				throw new IncorrectParameterException(new String[]{"hierarchy"});
			} else {								
				node = hierarchyRepository.findOne(hierarchyId);					
				if (node==null){
					throw new IncorrectParameterException(new String[]{"hierarchy"});
				}
			}
		}
		
		logger.info("WithPrice {"  + withPrice + " }");
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		
		if (auth!=null){
			String username = auth.getName();
			
			ObjectList<SerializableProduct> objects = findProduct(node,searchString,categories,withImagesString,withPrice,owned,ownedBy,wishedBy,store,seller,null,null,null,null,false);
			if (objects!=null && objects.getObjects()!=null && objects.getObjects().size()>0){
				
				
				Collection<SerializableProduct> products = objects.getObjects();
				Collection<ProductInfoConnector> connectors = connectorFactory.getConnectors(node, true);
				List<ScrapeRequest> scrapes = new ArrayList<>();
				

				for (ProductInfoConnector connector : connectors){
					String connectorId = connector.getIdentifier();
					for (SerializableProduct product: products){
						ScrapeRequest scrape = new ScrapeRequest();		
						scrape.setProductId(product.getId());
						scrape.setOnlyTransient(true);
						scrape.setLiveRequest(false);
						scrape.setConnector(connectorId);
						scrape.setUserId(username);
						scrape.setRequestTime(new Date());
						scrapes.add(scrape);						
					}
				}
				
				logger.info("SCRAPPING: " + scrapes);
				scrapeRequestRepository.save(scrapes);
				response.setStatus(HttpStatus.SC_PARTIAL_CONTENT);
			}
						
			
		}
		return Boolean.TRUE;	
	}
}

