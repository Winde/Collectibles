package model.dataobjects.validator;

import java.util.ArrayList;
import java.util.Collection;

import model.dataobjects.Category;

public class CategoryValidator extends DaoValidator<Category>{

	@Override
	public Collection<String> validate(Category e) {
		Collection<String> result = new ArrayList<>();
		if (e.getName()==null) {
			result.add("category.name");
		}
		return result;
	}

}
