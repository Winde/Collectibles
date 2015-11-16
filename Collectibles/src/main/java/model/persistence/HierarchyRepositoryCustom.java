package model.persistence;

import javax.transaction.Transactional;

import org.springframework.cache.annotation.CacheEvict;

import model.dataobjects.Category;
import model.dataobjects.HierarchyNode;

public interface HierarchyRepositoryCustom {

	@Transactional
	@CacheEvict(value="root", allEntries=true)
	public HierarchyNode addChild(HierarchyNode father,HierarchyNode child);
	
	@Transactional
	public HierarchyNode addCategory(HierarchyNode node,Category category);

	@Transactional
	public boolean removeCategory(HierarchyNode hierarchyNode,Category category);

	@Transactional
	@CacheEvict(value="root", allEntries=true)
	public HierarchyNode save(HierarchyNode node);

	@Transactional
	@CacheEvict(value="root", allEntries=true)
	public void deleteCascade(HierarchyNode node);

	@Transactional
	@CacheEvict(value="root", allEntries=true)
	public void deleteCascade(Long id);
}
