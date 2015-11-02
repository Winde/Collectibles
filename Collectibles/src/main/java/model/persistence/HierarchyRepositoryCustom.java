package model.persistence;

import javax.transaction.Transactional;

import model.dataobjects.Category;
import model.dataobjects.HierarchyNode;

public interface HierarchyRepositoryCustom {

	@Transactional
	public HierarchyNode addChild(HierarchyNode father,HierarchyNode child);
	
	@Transactional
	public HierarchyNode addCategory(HierarchyNode node,Category category);

	@Transactional
	public boolean removeCategory(HierarchyNode hierarchyNode,Category category);
}
