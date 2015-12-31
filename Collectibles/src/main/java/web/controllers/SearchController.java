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
import model.dataobjects.HierarchyNode;
import model.dataobjects.Product;
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
import org.springframework.web.bind.annotation.RestController;

import web.controllers.form.SearchForm;
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

	private final int defaultPaginationSize = 50;
	private final int maxPaginationSize = 150;
	
	private ObjectList<SerializableProduct> findProduct(SearchForm data, boolean forcePagination) throws CollectiblesException {

			ProductSearch searchObject = data.convertToDbRequest(userRepository);
		
			if (searchObject!=null && forcePagination){
				if (data.getPage()==null ) {
					data.setPage(0);
				}
				if (data.getMaxResults() == null || data.getMaxResults().equals(0) || data.getMaxResults() > maxPaginationSize){
					data.setMaxResults(defaultPaginationSize);					
				}				
			}
			
			ObjectList<Product> resultFromDB = null;

			if (searchObject!=null){
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
	public ObjectList<SerializableProduct> search(SearchForm data) throws CollectiblesException{					
		ObjectList<SerializableProduct> objects = findProduct(data,true);
		return objects;
	}
	
	@JsonView(ProductListView.class)
	@RequestMapping(value="/product/search/{hierarchy}/", method = RequestMethod.GET)
	public ObjectList<SerializableProduct> searchHierarchy(HttpServletRequest request, 
			@PathVariable String hierarchy, SearchForm data) throws CollectiblesException{					
		
		
		
		
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
		
		data.setHierarchy(node);
		
		ObjectList<SerializableProduct> objects = findProduct(data,true);		
		return objects;	
	}
	
	
	@Secured(value = { "ROLE_ADMIN" })
	@RequestMapping(value="/product/update/prices/{hierarchy}/", method = RequestMethod.POST)
	public Boolean updatePricesHierarchy(HttpServletResponse response,@PathVariable String hierarchy, SearchForm data) throws CollectiblesException{						
		
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
				
		data.setPage(null);
		data.setMaxResults(null);
		
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		
		if (auth!=null){
			String username = auth.getName();
			
			ObjectList<SerializableProduct> objects = findProduct(data,false);
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

