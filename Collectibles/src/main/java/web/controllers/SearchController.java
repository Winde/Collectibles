package web.controllers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import model.dataobjects.CategoryValue;
import model.dataobjects.HierarchyNode;
import model.dataobjects.Product;
import model.persistence.ProductRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import web.supporting.error.exceptions.CollectiblesException;
import web.supporting.error.exceptions.IncorrectParameterException;


@RestController
public class SearchController  extends CollectiblesController{

	@Autowired
	private ProductRepository productRepository;
	
	private Collection<Product> findProduct(String hierarchy, String search, Collection<String> categoryValuesIds) throws CollectiblesException {

			HierarchyNode node = null;
			if (hierarchy!=null && !"".equals(hierarchy.trim())){
				Long hierarchyId = null;
				try {
					hierarchyId = Long.parseLong(hierarchy);
				}catch (Exception ex) {
					throw new IncorrectParameterException(new String[]{"hierarchy"});
				}
				if (hierarchyId!=null){
					node = new HierarchyNode();
					node.setId(hierarchyId);
				}
			}
	
			List<CategoryValue> categoryValues = new ArrayList<>();
			if (categoryValuesIds!=null){
				for (String categoryValueString : categoryValuesIds){
					Long categoryId = null;
					try {
						categoryId = Long.parseLong(categoryValueString);
					}catch (Exception ex) {
						throw new IncorrectParameterException(new String[]{"categories"});
					}
					CategoryValue categoryValue = new CategoryValue();
					categoryValue.setId(categoryId);
					categoryValues.add(categoryValue);
				}
			}
			
			Collection<Product> result = null;
			if (node!=null){										//We search by HierarchyNode
				if (search!=null && !search.trim().equals("")){		//We have Search String
					if (categoryValues.size()>0){					//We have list of categories
						result = productRepository.searchProduct(node, search, categoryValues,new Long(categoryValues.size()) );
					} else {										//We don't have list of categories
						result = productRepository.searchProduct(node, search);
					}
				} else {											//We don't have a Search String
					if (categoryValues.size()>0){					//We have list of categories
						result = productRepository.searchProduct(node, categoryValues,new Long(categoryValues.size()));
					} else {										//We don't have list of categories
						result = productRepository.searchProduct(node);
					}
				}
			} else {												//We search without HierarchyNode
				if (search!=null && !search.trim().equals("")){		//We have Search String
					if (categoryValues.size()>0){					//We have categories ==> No category search allowed without hierarchy
						throw new IncorrectParameterException(new String[]{"categories"});
					} else {										//We don't have list of categories
						result = productRepository.searchProduct(search);
					}
				} else {
					//We don't have search string ==> As category search disallowed, we provided no search criteria
					throw new IncorrectParameterException(new String[]{"hierarchy","search","categories"});
				}
			}

			if (result ==null){
				return new ArrayList<>();
			} else {
				return result;
			}
	}
		
	@RequestMapping(value="/product/search")
	public Collection<Product> search(HttpServletRequest request, 			
			@RequestParam(required=false, name="search") String searchString
		) throws CollectiblesException{					
		return findProduct(null,searchString,null);
		
	}
	
	@RequestMapping(value="/product/search/{hierarchy}/")
	public Collection<Product> searchCategory(HttpServletRequest request, 
			@PathVariable String hierarchy, 
			@RequestParam(required=false, name="search") String searchString,
			@RequestParam(required=false, name="categoryValues" ) List<String> categories) throws CollectiblesException{					
		return findProduct(hierarchy,searchString,categories);
		
	}
}
