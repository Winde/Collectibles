package model.persistence;

import java.util.Collection;
import java.util.List;

import model.dataobjects.CategoryValue;
import model.dataobjects.HierarchyNode;
import model.dataobjects.Product;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends CrudRepository<Product,Long>{

	public List<Product> findAll();
	
	public Product findOne(Long id);
	
	@Query("select p from Product p where p.reference = ?1")
	public Product findOneByReference(String reference);

	@Query("select p from Product p INNER JOIN p.categoryValues categoryValues "
			+ "where "
			+ "( "
			+ "		lower(p.description) LIKE lower(CONCAT('%',:search,'%')) OR "
			+ "		lower(p.name) LIKE lower(CONCAT('%',:search,'%'))  "
			+ ") "
			+ "AND "
			+ "(p.hierachyPlacement = :node) "
			+ "AND "
			+ "( categoryValues IN (:categoryValues) ) "
			+ "group by p having count(categoryValues)=:sizeCategoryValues ")
	public List<Product> searchProduct(
										@Param(value = "node") HierarchyNode node,
										@Param(value = "search") String search,
										@Param(value = "categoryValues") Collection<CategoryValue> categoryValues,
										@Param(value = "sizeCategoryValues") Long sizeCategoryValues);
	
	@Query("select p from Product p "
			+ "where "
			+ "( "
			+ "		lower(p.description) LIKE lower(CONCAT('%',:search,'%')) OR "
			+ "		lower(p.name) LIKE lower(CONCAT('%',:search,'%'))  "
			+ ") "
			+ "AND "
			+ "(p.hierachyPlacement = :node) ")
	public List<Product> searchProduct(@Param(value = "node") HierarchyNode node,@Param(value = "search") String search);
		
	@Query("select p from Product p INNER JOIN p.categoryValues categoryValues "
			+ "where "
			+ "(p.hierachyPlacement = :node) "
			+ "AND "
			+ "( categoryValues IN (:categoryValues) ) "
			+ "group by p having count(categoryValues)=:sizeCategoryValues ")	
	public List<Product> searchProduct(
										@Param(value = "node") HierarchyNode node,
										@Param(value = "categoryValues") Collection<CategoryValue> categoryValues,
										@Param(value = "sizeCategoryValues") Long sizeCategoryValues);
	
	@Query("select p from Product p "
			+ "where "
			+ "( "
			+ "		lower(p.description) LIKE lower(CONCAT('%',:search,'%')) OR "
			+ "		lower(p.name) LIKE lower(CONCAT('%',:search,'%'))  "
			+ ") ")
	public List<Product> searchProduct(@Param(value = "search")String search);
	
	@Query("select p from Product p "
			+ "where "
			+ "(p.hierachyPlacement = :node) ")
	public List<Product> searchProduct(@Param(value = "node") HierarchyNode node);
}
