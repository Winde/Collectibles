package web.controllers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import model.dataobjects.CategoryValue;
import model.dataobjects.HierarchyNode;
import model.dataobjects.Product;
import model.dataobjects.User;
import model.dataobjects.serializable.SerializableProduct;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import web.supporting.error.exceptions.CollectiblesException;
import web.supporting.error.exceptions.IncorrectParameterException;

import com.fasterxml.jackson.annotation.JsonView;


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
			String withDriveThruLinkString,
			String ownedString,
			int page,
			int maxResults) throws CollectiblesException {

			Boolean withImages = null;			
			Boolean withPrice = null;
			Boolean withDriveThruLink = null;
			
			
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
			
			if (withDriveThruLinkString!=null && withDriveThruLinkString.equals("true")){
				withDriveThruLink = Boolean.TRUE;
			} else if (withDriveThruLinkString!=null && withDriveThruLinkString.equals("false")){
				withDriveThruLink = Boolean.FALSE;
			}
			
			searchObject.setWithDriveThruRPGLink(withDriveThruLink);
			
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
						
			ObjectList<Product> resultFromDB = null;

			Collection<String> errors = searchObject.errors();
			if (errors == null || errors.size()<=0){
				resultFromDB = productRepository.searchProduct(searchObject);
			}else {
				throw new IncorrectParameterException(errors);
			}
			
			
			
			ObjectList<SerializableProduct> result = null;

			logger.info("Result: " + resultFromDB);
			if (resultFromDB!=null && resultFromDB.getObjects()!=null) {
				logger.info("Result objects number: " + resultFromDB.getObjects().size());
				result = new ObjectList<>();
				result.setHasNext(resultFromDB.getHasNext());
				result.setMaxResults(resultFromDB.getMaxResults());				
				result.setObjects(SerializableProduct.cloneProduct(resultFromDB.getObjects()));				
			}
			
			return result;
	}
		
	@JsonView(model.dataobjects.serializable.SerializableProduct.ProductListView.class)	
	@RequestMapping(value="/product/search")
	public ObjectList<SerializableProduct> search(HttpServletRequest request, 			
			@RequestParam(required=false, name="withImages") String withImagesString,
			@RequestParam(required=false, name="search") String searchString,
			@RequestParam(required=false, name="owned" ) String owned,
			@RequestParam(required=false, name="withPrice") String withPrice,
			@RequestParam(required=false, name="withDriveThruLink") String withDriveThruLink,
			@RequestParam(required=true, name="page") int page,
			@RequestParam(required=false, name="maxResults") int maxResults) throws CollectiblesException{	
		ObjectList<SerializableProduct> objects = findProduct(null,searchString,null,withImagesString,withPrice,withDriveThruLink,owned,page,maxResults);
		return objects;
	}
	
	@JsonView(ProductListView.class)
	@RequestMapping(value="/product/search/{hierarchy}/")
	public ObjectList<SerializableProduct> searchCategory(HttpServletRequest request, 
			@PathVariable String hierarchy, 
			@RequestParam(required=false, name="search") String searchString,
			@RequestParam(required=false, name="withImages") String withImagesString,
			@RequestParam(required=false, name="owned" ) String owned,
			@RequestParam(required=false, name="withPrice") String withPrice,
			@RequestParam(required=false, name="withDriveThruLink") String withDriveThruLink,
			@RequestParam(required=false, name="categoryValues" ) List<String> categories,
			@RequestParam(required=true, name="page") int page,
			@RequestParam(required=false, name="maxResults", defaultValue=defaultPaginationSize) int maxResults) throws CollectiblesException{					
		ObjectList<SerializableProduct> objects = findProduct(hierarchy,searchString,categories,withImagesString,withPrice,withDriveThruLink,owned,page,maxResults);
		return objects;
		
	}
}

