package model.persistence;

import java.util.List;

import javax.persistence.QueryHint;

import model.dataobjects.Product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product,Long>, ProductRepositoryCustom{
	
	@QueryHints({ @QueryHint(name = "org.hibernate.fetchSize", value ="200") })
	@Query("select p from Product p")
	public List<Product> findAll();
	
	public Product findOne(Long id);

}
