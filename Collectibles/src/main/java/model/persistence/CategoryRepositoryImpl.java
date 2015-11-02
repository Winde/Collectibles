package model.persistence;

import javax.transaction.Transactional;

import model.dataobjects.Category;
import model.dataobjects.CategoryValue;
import model.dataobjects.Product;

import org.springframework.beans.factory.annotation.Autowired;

public class CategoryRepositoryImpl implements CategoryRepositoryCustom{

	@Autowired
	private CategoryRepository categoryRepository;
	
	@Autowired
	private CategoryValueRepository categoryValueRepository;

	@Transactional
	public Category addValue(Category category, CategoryValue categoryValue) {
		categoryValue.setCategory(category);
		categoryValueRepository.save(categoryValue);
		category.addCategoryValue(categoryValue);
		categoryRepository.save(category);
		return category;
	}
	
	
	
	
}
