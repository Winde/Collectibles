package model.persistence;

import java.util.Collection;
import java.util.List;

import model.dataobjects.CategoryValue;
import model.dataobjects.HierarchyNode;
import model.dataobjects.Image;
import model.dataobjects.Product;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends CrudRepository<Product,Long>, ProductRepositoryCustom{

	public List<Product> findAll();
	
	public Product findOne(Long id);
	
	@Query("select p from Product p where p.reference = ?1")
	public Product findOneByReference(String reference);

		
}
