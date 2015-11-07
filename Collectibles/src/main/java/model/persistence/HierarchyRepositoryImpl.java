package model.persistence;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;

import model.dataobjects.Category;
import model.dataobjects.HierarchyNode;

public class HierarchyRepositoryImpl implements HierarchyRepositoryCustom{

	@Autowired
	private HierarchyRepository hierarchyRepository;
	
	@Autowired
	private CategoryRepository categoryRepository;
	
	@PersistenceContext
	private EntityManager entityManager;
	
	
	public HierarchyNode addChild(HierarchyNode father,HierarchyNode child ) {		
		child.setFather(father);		
		this.save(child);		
		father.addChildren(child);		
		this.save(father);		
		return father;
	}
	
	public HierarchyNode addCategory(HierarchyNode hierarchyNode,Category category){
		category.addHierarchy(hierarchyNode);
		categoryRepository.save(category);
		hierarchyNode.addCategory(category);
		this.save(hierarchyNode);		
		return hierarchyNode;
	}
	
	public boolean removeCategory(HierarchyNode hierarchyNode,Category category){
		boolean result = true;
		result = result && hierarchyNode.removeCategory(category);
		result = result && category.removeHierarchy(hierarchyNode);
		this.save(hierarchyNode);
		categoryRepository.save(category);
		return result;
	}
	
	@Override 
	public HierarchyNode save(HierarchyNode node){	
		System.out.println("BEGIN:" + node);
		HierarchyNode result = null;
		if (node.getId()!=null && node.getLineage()!=null){
			result = entityManager.merge(node);
		} else {
			entityManager.persist(node);
			entityManager.refresh(node);
			boolean updateLineage = node.updateLineage();
			
			System.out.println(node.getId());
			System.out.println(node.getLineage());
			
			if (updateLineage){
				result = entityManager.merge(node);
			} else {
				result = node;
			}
		}
		entityManager.flush();
		System.out.println("END:" + node);
		return result;		
	}
	
}
