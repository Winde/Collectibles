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
import model.dataobjects.supporting.ObjectList;
import model.persistence.queryParameters.ProductSearch;

public interface ProductRepositoryCustom {

	
	@Transactional
	public Product addImage(Product product, Image image);
	
	@Transactional
	public Product addImage(Product product, Collection<Image> images);
	
	@Transactional
	boolean removeImage(Product product, Long imageId);
	
	public Collection<Product> searchProduct(HierarchyNode node);
	
	public ObjectList<Product> searchProduct(ProductSearch search);
	
	@Transactional
	public void saveWithImages(Collection<Product> products,Collection<Image> images);
	
	@Transactional
	public void saveWithImages(Product product,Collection<Image> images);
	
	@Transactional
	public boolean mergeAndSaveProductWithoutImages(Product product, Collection<Image> image);


	

}
