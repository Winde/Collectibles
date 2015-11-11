package model.dataobjects.validator;

import java.util.ArrayList;
import java.util.Collection;

import model.dataobjects.Product;

public class ProductValidator extends DaoValidator<Product>{

	@Override
	public Collection<String> validate(Product e) {
		Collection<String> result = new ArrayList<>();
		if (e.getHierarchyPlacement()==null){
			result.add("product.hierarchyPlacement");
		}
		if (e.getName()==null){
			result.add("product.name");
		}
		
		if (e.getUniversalReference()!=null && "".equals(e.getUniversalReference().trim())){
			result.add("product.amazonReference");
		}
		return result;
	}

}
