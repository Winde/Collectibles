package web.controllers;

import java.util.Collection;

import model.dataobjects.Category;
import model.dataobjects.CategoryValue;
import model.dataobjects.HierarchyNode;
import model.dataobjects.Product;
import model.persistence.CategoryRepository;
import model.persistence.HierarchyRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import web.supporting.error.exceptions.CollectiblesException;
import web.supporting.error.exceptions.GenericException;
import web.supporting.error.exceptions.IncorrectParameterException;
import web.supporting.error.exceptions.NotFoundException;

@RestController
public class CategoryController extends CollectiblesController{

	@Autowired
	private HierarchyRepository hierarchyRepository;
	
	@Autowired
	private CategoryRepository categoryRepository;
	

	@RequestMapping(value="/category/find/{id}")
	public Category product(@PathVariable String id) throws CollectiblesException {
		Long idLong = null;
		try {
			idLong = Long.parseLong(id);
		}catch(Exception ex){
			
		}
		if (idLong==null){
			throw new IncorrectParameterException(new String[]{"id"});
		} else {
			Category category = categoryRepository.findOne(idLong);
			if (category==null){
				throw new NotFoundException();
			} else {
				return category;
			}
		}
	}
	
	@RequestMapping("/category/by/hierarchy/{hierarchy}")
	public Collection<Category> categories(@PathVariable String hierarchy) throws CollectiblesException{
		Long hierarchyId = null;
		try {
			hierarchyId = Long.parseLong(hierarchy);
		}catch(Exception ex){}
		
		if (hierarchyId==null){
			throw new IncorrectParameterException(new String[]{"hierarchy"});
		} else {
			HierarchyNode hierarchyNode = hierarchyRepository.findOne(hierarchyId);
			if (hierarchyNode==null){
				throw new NotFoundException(new String[]{"hierarchy"});
			} else {
				return hierarchyNode.getCategories();
			}
		}
		
	}
	
	@RequestMapping(value="/category/add/{hierarchy}", method = RequestMethod.POST)
	public Category addCategory(@PathVariable String hierarchy,@RequestBody Category category) throws CollectiblesException {		
		Long hierarchyId = null;
		try {
			hierarchyId = Long.parseLong(hierarchy);
		}catch(Exception ex){}
		
		if (hierarchyId==null){
			throw new IncorrectParameterException(new String[]{"hierarchy"});
		} else {
			this.validate(category);
			
			HierarchyNode hierarchyNode = hierarchyRepository.findOne(hierarchyId);			
			if (hierarchyNode==null) {
				throw new NotFoundException(new String[]{"hierarchy"});	
			} else {
				category.setId(null);
				hierarchyRepository.addCategory(hierarchyNode, category);
				return category;
			}
		}
	}
	
	@RequestMapping(value="/category/remove/{category}", method = RequestMethod.POST)
	public boolean removeCategory(@PathVariable String category) throws CollectiblesException {		
		Long categoryId = null;
		try {
			categoryId = Long.parseLong(category);
		}catch(Exception ex){}
		
		if (categoryId==null){
			throw new IncorrectParameterException(new String[]{"category"});
		} else {
			try {
				categoryRepository.delete(categoryId);			
			}catch(EmptyResultDataAccessException ex) {				
				throw new NotFoundException(new String[]{"category"});
			}
			return true;
		}
	}
	
	@RequestMapping(value="/category/{category}/dettach/{hierarchy}", method = RequestMethod.POST)
	public boolean  removeCategoryFromHierarchy(@PathVariable String category, @PathVariable String hierarchy ) throws CollectiblesException {		
		Long categoryId = null;
		try {
			categoryId = Long.parseLong(category);
		}catch(Exception ex){}
		
		Long hierarchyId = null;
		try {
			hierarchyId = Long.parseLong(hierarchy);
		}catch(Exception ex){}
		
		if (categoryId==null && hierarchyId==null){
			throw new IncorrectParameterException(new String[]{"category","hierarchy"});
		} else if (categoryId == null){
			throw new IncorrectParameterException(new String[]{"category"});
		} else if( hierarchyId == null) {
			throw new IncorrectParameterException(new String[]{"hierarchy"});
		} else {
			HierarchyNode hierarchyInDb = hierarchyRepository.findOne(hierarchyId);
			Category categoryInDb = categoryRepository.findOne(categoryId);
			if (hierarchyInDb ==null && categoryInDb==null) {
				throw new NotFoundException(new String[]{"hierarchy","category"});	
			}else if (hierarchyInDb == null){
				throw new NotFoundException(new String[]{"hierarchy"});	
			}else if (categoryInDb == null){
				throw new NotFoundException(new String[]{"category"});	
			}else {
				return hierarchyRepository.removeCategory(hierarchyInDb, categoryInDb);	
			} 			
		}
	}
	
	@RequestMapping(value="/category/add/value/to/{category}", method = RequestMethod.POST)
	public CategoryValue addCategobyryValue(@PathVariable String category, @RequestBody CategoryValue categoryValue) throws CollectiblesException {		
		Long categoryId = null;
		try {
			categoryId = Long.parseLong(category);
		}catch(Exception ex){}
		
		if (categoryId==null){
			throw new IncorrectParameterException(new String[]{"category"});			
		} else {
			this.validate(categoryValue);
			
			Category categoryInDb = categoryRepository.findOne(categoryId);
			if (categoryInDb==null){
				throw new NotFoundException(new String[]{"category"});			
			} else {
				categoryValue.setId(null);
				categoryRepository.addValue(categoryInDb, categoryValue);
				return categoryValue;
			}
		}
	}
	
}
