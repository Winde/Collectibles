package model.persistence;

import java.util.Collection;

import javax.transaction.Transactional;

import model.dataobjects.Image;
import model.dataobjects.Product;

public interface ProductRepositoryCustom {

	
	@Transactional
	public Product addImage(Product product, Image image);
	
	@Transactional
	public Product addImage(Product product, Collection<Image> images);
	
	@Transactional
	boolean removeImage(Product product, Long imageId);
}
