package web.controllers;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import model.dataobjects.CategoryValue;
import model.dataobjects.HierarchyNode;
import model.dataobjects.Product;
import model.dataobjects.User;
import model.dataobjects.serializable.SerializableProduct;
import model.dataobjects.serializable.SerializableProduct.ProductComplexView;
import model.dataobjects.serializable.SerializableProduct.ProductListView;
import model.dataobjects.supporting.ObjectList;
import model.persistence.HierarchyRepository;
import model.persistence.ProductRepository;
import model.persistence.UserRepository;
import model.persistence.queryParameters.ProductSearch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
	
	private final String defaultPaginationSize = "50";
	
	private ObjectList<SerializableProduct> findProduct(
			String hierarchy, 
			String search, 
			Collection<String> categoryValuesIds, 
			String withImagesString,
			String withPriceString,
			String ownedString,
			String ownedByString,
			String sortBy,
			String sortOrder,
			int page,
			int maxResults) throws CollectiblesException {

			Boolean withImages = null;			
			Boolean withPrice = null;

			boolean errorReturnNoResults = false;
			ProductSearch searchObject = new ProductSearch();
			
			searchObject.setSearchTerm(search);
			searchObject.setPage(page);
			searchObject.setMaxResults(maxResults);
		

			Boolean owned = null;
			
			if (ownedString!=null && ownedString.equals("true")){
				owned = Boolean.TRUE;
			} else if (ownedString!=null && ownedString.equals("false")){
				owned = Boolean.FALSE;
			}
			
			searchObject.setOwned(owned);
			if (ownedString!=null){
				Authentication auth = SecurityContextHolder.getContext().getAuthentication();				
				
				User owner = userRepository.findOne(auth.getName());
				searchObject.setOwner(owner);
			}
			
			if (ownedByString!=null && !ownedByString.trim().equals("")){
				Long userId = null;
				try {
					userId = Long.parseLong(ownedByString);
				}catch (Exception ex){
					ex.printStackTrace();
				}
				if (userId!=null){
					User user = userRepository.findById(userId);				
					if (searchObject.getOwner()!=null){
						if (!searchObject.getOwner().equals(user) || searchObject.getOwned()==null || !searchObject.getOwned().equals(Boolean.TRUE)){
							errorReturnNoResults = true;					
						}
					}
					searchObject.setOwner(user);
					searchObject.setOwned(Boolean.TRUE);
					if (user==null){
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
			
			searchObject.setHierarchy(node);
			
						
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
			@RequestParam(required=false, name="withPrice") String withPrice,
			@RequestParam(required=false, name="sortBy") String sortBy,
			@RequestParam(required=false, name="sortOrder") String sortOrder,
			@RequestParam(required=true, name="page") int page,
			@RequestParam(required=false, name="maxResults") int maxResults) throws CollectiblesException{	
		ObjectList<SerializableProduct> objects = findProduct(null,searchString,null,withImagesString,withPrice,owned,ownedBy,sortBy,sortOrder,page,maxResults);
		return objects;
	}
	
	@JsonView(ProductListView.class)
	@RequestMapping(value="/product/search/{hierarchy}/", method = RequestMethod.GET)
	public ObjectList<SerializableProduct> searchCategory(HttpServletRequest request, 
			@PathVariable String hierarchy, 
			@RequestParam(required=false, name="search") String searchString,
			@RequestParam(required=false, name="withImages") String withImagesString,
			@RequestParam(required=false, name="owned" ) String owned,
			@RequestParam(required=false, name="ownedBy" ) String ownedBy,
			@RequestParam(required=false, name="withPrice") String withPrice,
			@RequestParam(required=false, name="categoryValues" ) List<String> categories,
			@RequestParam(required=false, name="sortBy") String sortBy,
			@RequestParam(required=false, name="sortOrder") String sortOrder,
			@RequestParam(required=true, name="page") int page,
			@RequestParam(required=false, name="maxResults", defaultValue=defaultPaginationSize) int maxResults) throws CollectiblesException{					
		ObjectList<SerializableProduct> objects = findProduct(hierarchy,searchString,categories,withImagesString,withPrice,owned,ownedBy,sortBy,sortOrder,page,maxResults);
		return objects;
		
	}
}

