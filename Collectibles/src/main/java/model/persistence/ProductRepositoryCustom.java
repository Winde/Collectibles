package model.persistence;

import java.util.Collection;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import model.dataobjects.CategoryValue;
import model.dataobjects.HierarchyNode;
import model.dataobjects.Image;
import model.dataobjects.Product;

public interface ProductRepositoryCustom {

	
	@Transactional
	public Product addImage(Product product, Image image);
	
	@Transactional
	public Product addImage(Product product, Collection<Image> images);
	
	@Transactional
	boolean removeImage(Product product, Long imageId);

	public List<Product> searchProduct(HierarchyNode node);
	
	public List<Product> searchProduct(String search);
	
	public List<Product> searchProduct(HierarchyNode node,String search);
	
	public List<Product> searchProduct(HierarchyNode node, String search, Collection<CategoryValue> categoryValues, Boolean withImages);
	
}
