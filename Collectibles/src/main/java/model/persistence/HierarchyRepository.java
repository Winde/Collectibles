package model.persistence;

import java.util.List;

import model.dataobjects.HierarchyNode;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HierarchyRepository extends CrudRepository<HierarchyNode,Long>, HierarchyRepositoryCustom{

	public List<HierarchyNode> findAll();
	
	public HierarchyNode findOne(Long id);
	
	@Cacheable(value="root", cacheManager="cacheManager")
	@Query("select h from HierarchyNode h where h.father IS NULL")
	public HierarchyNode findRoot();

	@CacheEvict(value="root", allEntries=true)
	public HierarchyNode save(HierarchyNode entity);	
	
	@CacheEvict(value="root", allEntries=true)
	public <S extends HierarchyNode>Iterable<S> save(Iterable<S> entities);
	
	@CacheEvict(value="root", allEntries=true)
	public void delete(Iterable<? extends HierarchyNode> entities);
	
	@CacheEvict(value="root", allEntries=true)
	public void delete(HierarchyNode entity);
	
	@CacheEvict(value="root", allEntries=true)
	public void delete(Long id);
	
	@CacheEvict(value="root", allEntries=true)
	public void deleteAll();
}
