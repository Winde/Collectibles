package web.controllers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import model.dataobjects.CategoryValue;
import model.dataobjects.HierarchyNode;
import model.dataobjects.Product;
import model.dataobjects.Product.ProductListView;
import model.persistence.HierarchyRepository;
import model.persistence.ProductRepository;
import model.persistence.queryParameters.ProductSearch;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import web.supporting.error.exceptions.CollectiblesException;
import web.supporting.error.exceptions.IncorrectParameterException;

import com.fasterxml.jackson.annotation.JsonView;


@RestController
public class SearchController  extends CollectiblesController{

	@Autowired
	private ProductRepository productRepository;
	
	@Autowired
	private HierarchyRepository hierarchyRepository;
	
	
	private Collection<Product> findProduct(
			String hierarchy, 
			String search, 
			Collection<String> categoryValuesIds, 
			String withImagesString,
			String ownedString) throws CollectiblesException {

			Boolean withImages = null;
		
			
			ProductSearch searchObject = new ProductSearch();
			
			searchObject.setSearchTerm(search);
			

			Boolean owned = null;
			
			if (ownedString!=null && ownedString.equals("true")){
				owned = Boolean.TRUE;
			} else if (ownedString!=null && ownedString.equals("false")){
				owned = Boolean.FALSE;
			}
			
			searchObject.setOwned(owned);
			
			if (withImagesString!=null && withImagesString.equals("true")){
				withImages = Boolean.TRUE;
			} else if (withImagesString!=null && withImagesString.equals("false")){
				withImages = Boolean.FALSE;
			}
			
			searchObject.setWithImages(withImages);
		
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
						
			Collection<Product> result = null;

			Collection<String> errors = searchObject.errors();
			if (errors == null || errors.size()<=0){
				result = productRepository.searchProduct(searchObject);
			}else {
				throw new IncorrectParameterException(errors);
			}

			if (result ==null){
				return new ArrayList<>();
			} else {				
				return result;
			}
	}
		
	@JsonView(ProductListView.class)	
	@RequestMapping(value="/product/search")
	public Collection<Product> search(HttpServletRequest request, 			
			@RequestParam(required=false, name="withImages") String withImagesString,
			@RequestParam(required=false, name="search") String searchString,
			@RequestParam(required=false, name="owned" ) String owned) throws CollectiblesException{					
		return findProduct(null,searchString,null,withImagesString,owned);
		
	}
	
	@JsonView(ProductListView.class)
	@RequestMapping(value="/product/search/{hierarchy}/")
	public Collection<Product> searchCategory(HttpServletRequest request, 
			@PathVariable String hierarchy, 
			@RequestParam(required=false, name="search") String searchString,
			@RequestParam(required=false, name="withImages") String withImagesString,
			@RequestParam(required=false, name="owned" ) String owned,
			@RequestParam(required=false, name="categoryValues" ) List<String> categories) throws CollectiblesException{					
		return findProduct(hierarchy,searchString,categories,withImagesString,owned);
		
	}
}
