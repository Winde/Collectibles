package model.dataobjects.validator;

import java.util.ArrayList;
import java.util.Collection;

import model.dataobjects.HierarchyNode;

public class HierarchyValidator extends DaoValidator<HierarchyNode>{

	@Override
	public Collection<String> validate(HierarchyNode e) {
			Collection<String> result = new ArrayList<>();
			if (e.getName()==null){
				result.add("hierarchy.name");
			}
			return result;
	}

}
