package model.persistence;

import model.dataobjects.Category;
import model.dataobjects.CategoryValue;

public interface CategoryRepositoryCustom {

	public Category addValue(Category category,CategoryValue value);
}
