package model.persistence;

import java.util.List;

import model.dataobjects.Product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import javax.persistence.QueryHint;

@Repository
public interface ProductRepository extends JpaRepository<Product,Long>, ProductRepositoryCustom{
	
	@QueryHints({ @QueryHint(name = "org.hibernate.fetchSize", value ="200") })	
	public List<Product> findAll();
	
	public Product findOne(Long id);

}
