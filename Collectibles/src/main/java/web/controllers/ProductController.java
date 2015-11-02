package web.controllers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import model.dataobjects.Category;
import model.dataobjects.CategoryValue;
import model.dataobjects.HierarchyNode;
import model.dataobjects.Product;
import model.persistence.CategoryValueRepository;
import model.persistence.HierarchyRepository;
import model.persistence.ProductRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import web.supporting.error.exceptions.CollectiblesException;
import web.supporting.error.exceptions.IncorrectParameterException;
import web.supporting.error.exceptions.NotFoundException;


@RestController
public class ProductController  extends CollectiblesController{

	@Autowired
	private ProductRepository productRepository;
	
	@Autowired
	private CategoryValueRepository categoryValueRepository;
	
	@Autowired
	private HierarchyRepository hierarchyRepository;

	@RequestMapping(value="/product/find/{id}")
	public Product product(@PathVariable String id) throws CollectiblesException {
		Long idLong = null;
		try {
			idLong = Long.parseLong(id);
		}catch(Exception ex){
			
		}
		if (idLong==null){
			throw new IncorrectParameterException(new String[]{"id"});
		} else {
			Product product = productRepository.findOne(idLong);
			if (product==null){
				throw new NotFoundException();
			} else {
				return product;
			}
		}
	}
	
	@RequestMapping(value="/product/create/{hierarchy}", method = RequestMethod.POST)
	public Product addProduct(@RequestBody Product product,@PathVariable String hierarchy) throws CollectiblesException {
		Long hierarchyId = null;
		try {
			hierarchyId = Long.parseLong(hierarchy);
		}catch(Exception ex){
			
		}
		if (hierarchyId!=null){
			
			HierarchyNode hierarchyNode = hierarchyRepository.findOne(hierarchyId);
			if (hierarchyNode!=null){
				product.setId(null);
				product.setHierachyPlacement(hierarchyNode);
				this.validate(product);				
				productRepository.save(product);
				return product;
			} else {
				throw new NotFoundException(new String[]{"hierarchy"});
			}
		} else {
			throw new IncorrectParameterException(new String[]{"hierarchy"});
		}		
		
	}
	
	@RequestMapping(value="/product/remove/{id}", method = RequestMethod.POST)
	public Long removeProduct(@PathVariable String id) throws CollectiblesException {		
		Long idLong = null;
		try {
			idLong = Long.parseLong(id);
		}catch(Exception ex){
			
		}
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
	
	
	@RequestMapping(value="/product/find/{productId}/category/value/add/{categoryValueId}", method = RequestMethod.POST)
	public Product addCategoryValue(@PathVariable String productId, @PathVariable String categoryValueId) throws CollectiblesException {
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
		Collection<Category> productCategories = product.getHierachyPlacement().getCategories();
		
		System.out.println(category);
		System.out.println(productCategories);
		
		if (productCategories == null || category == null || !productCategories.contains(category)){
			throw new IncorrectParameterException(new String[]{"category"});
		}
		
		product.addCategoryValue(categoryValue);
		productRepository.save(product);		
		System.out.println("HERE");
		return product;
	}
	
	@RequestMapping(value="/product/modify/", method = RequestMethod.POST)
	public Product addProduct(@RequestBody Product product) throws CollectiblesException {		
		this.validate(product);
		
		if (product.getId()!=null){
			Product productInDb = productRepository.findOne(product.getId());
			if (productInDb!=null){
				product.setHierachyPlacement(productInDb.getHierachyPlacement());
				product.setCategoryValues(productInDb.getCategoryValues());				
				Product result = productRepository.save(product);
				return result;
			} else {
				throw new NotFoundException(new String[]{"product"});
			}
		} else {
			throw new IncorrectParameterException(new String[]{"product.id"});
		}
				
	}
}
