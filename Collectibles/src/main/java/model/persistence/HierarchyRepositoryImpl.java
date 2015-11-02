package model.persistence;

import org.springframework.beans.factory.annotation.Autowired;

import model.dataobjects.Category;
import model.dataobjects.HierarchyNode;

public class HierarchyRepositoryImpl implements HierarchyRepositoryCustom{

	@Autowired
	private HierarchyRepository hierarchyRepository;
	
	@Autowired
	private CategoryRepository categoryRepository;
	
	
	public HierarchyNode addChild(HierarchyNode father,HierarchyNode child ) {
		child.setFather(father);
		hierarchyRepository.save(child);
		father.addChildren(child);
		hierarchyRepository.save(father);
		return father;
	}
	
	public HierarchyNode addCategory(HierarchyNode hierarchyNode,Category category){
		category.addHierarchy(hierarchyNode);
		categoryRepository.save(category);
		hierarchyNode.addCategory(category);
		hierarchyRepository.save(hierarchyNode);		
		return hierarchyNode;
	}
	
	public boolean removeCategory(HierarchyNode hierarchyNode,Category category){
		boolean result = true;
		result = result && hierarchyNode.removeCategory(category);
		result = result && category.removeHierarchy(hierarchyNode);
		hierarchyRepository.save(hierarchyNode);
		categoryRepository.save(category);
		return result;
	}
	
}
