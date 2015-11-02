package model.dataobjects.validator;

import java.util.Collection;
import model.dataobjects.*;

public abstract class DaoValidator<E> {

	public abstract Collection<String> validate(E e);

	public static <T> DaoValidator<T> getValidator(T t){
		if (t.getClass().isAssignableFrom(Category.class)){
			return (DaoValidator<T>) new CategoryValidator();
		} else if(t.getClass().isAssignableFrom(CategoryValue.class)) {
			return (DaoValidator<T>) new CategoryValueValidator();
		} else if(t.getClass().isAssignableFrom(HierarchyNode.class)) {
			return (DaoValidator<T>) new HierarchyValidator();
		} else if(t.getClass().isAssignableFrom(Product.class)) {
			return (DaoValidator<T>) new ProductValidator();
		}
		return null;
	}
}
