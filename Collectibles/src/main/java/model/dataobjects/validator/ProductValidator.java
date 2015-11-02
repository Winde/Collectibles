package model.dataobjects.validator;

import java.util.ArrayList;
import java.util.Collection;

import model.dataobjects.Product;

public class ProductValidator extends DaoValidator<Product>{

	@Override
	public Collection<String> validate(Product e) {
		Collection<String> result = new ArrayList<>();
		if (e.getHierachyPlacement()==null){
			result.add("product.hierarchyPlacement");
		}
		if (e.getName()==null){
			result.add("product.name");
		}
		return result;
	}

}
