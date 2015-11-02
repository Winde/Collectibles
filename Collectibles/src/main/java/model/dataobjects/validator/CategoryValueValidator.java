package model.dataobjects.validator;

import java.util.ArrayList;
import java.util.Collection;

import model.dataobjects.Category;
import model.dataobjects.CategoryValue;

public class CategoryValueValidator extends DaoValidator<CategoryValue>{

	@Override
	public Collection<String> validate(CategoryValue e) {
		Collection<String> result = new ArrayList<>();
		if (e.getName()==null) {
			result.add("categoryValue.name");
		}
		return result;
	}

}
