package model.persistence;

import java.security.InvalidParameterException;
import java.util.Collection;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;

import model.dataobjects.Category;
import model.dataobjects.HierarchyNode;
import model.dataobjects.Product;

public class HierarchyRepositoryImpl implements HierarchyRepositoryCustom{

	@Autowired
	private HierarchyRepository hierarchyRepository;
	
	@Autowired
	private CategoryRepository categoryRepository;
	
	@Autowired
	private ProductRepository productRepository;
	
	
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
		//hierarchyNode.addCategory(category);
		this.save(hierarchyNode);		
		return hierarchyNode;
	}
	
	public boolean removeCategory(HierarchyNode hierarchyNode,Category category){
		boolean result = true;
		//result = result && hierarchyNode.removeCategory(category);
		result = result && category.removeHierarchy(hierarchyNode);
		this.save(hierarchyNode);
		categoryRepository.save(category);
		return result;
	}
	
	@Override
	public void deleteCascade(Long id) throws InvalidParameterException{		
		HierarchyNode node = hierarchyRepository.findOne(id);
		if (node!=null && node.getFather()!=null){
			this.deleteCascade(node);
		} else if (node!=null && node.getFather()==null){
			throw new InvalidParameterException("Can't delete root");
		}
	}
	
	@Override
	public void deleteCascade(HierarchyNode node){		
		Collection<Product> products = productRepository.searchProduct(node);
		productRepository.delete(products);
		
		if (node.getChildren()!=null && node.getChildren().size()>0){			
			for (HierarchyNode child : node.getChildren()){
				this.deleteCascade(child);				
			}
		}
		
		node.getFather().removeChildren(node);
		
		hierarchyRepository.delete(node);		
	}
	
	@Override 
	public HierarchyNode save(HierarchyNode node){			
		HierarchyNode result = null;
		if (node.getId()!=null && node.getLineage()!=null){
			result = entityManager.merge(node);
		} else {
			entityManager.persist(node);
			entityManager.refresh(node);
			boolean updateLineage = node.updateLineage();				
			if (updateLineage){
				result = entityManager.merge(node);
			} else {
				result = node;
			}
		}
		entityManager.flush();		
		return result;		
	}
	
}
