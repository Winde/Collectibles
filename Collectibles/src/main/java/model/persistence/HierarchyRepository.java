package model.persistence;

import java.util.List;

import model.dataobjects.Category;
import model.dataobjects.HierarchyNode;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HierarchyRepository extends CrudRepository<HierarchyNode,Long>, HierarchyRepositoryCustom{

	public List<HierarchyNode> findAll();
	
	public HierarchyNode findOne(Long id);
	
	@Query("select h from HierarchyNode h where h.father IS NULL")
	public HierarchyNode findRoot();	
}
