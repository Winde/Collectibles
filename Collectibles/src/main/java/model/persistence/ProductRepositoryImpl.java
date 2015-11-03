package model.persistence;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;

import model.dataobjects.Image;
import model.dataobjects.Product;

public class ProductRepositoryImpl implements ProductRepositoryCustom{

	@Autowired
	private ImageRepository imageRepository;
	
	@Autowired
	private ProductRepository productRepository;
	
	@Override
	public Product addImage(Product product, Image image) {		
		imageRepository.save(image);
		product.addImage(image);	
		productRepository.save(product);
		return product;
	}

	@Override
	public Product addImage(Product product, Collection<Image> images) {
		imageRepository.save(images);
		for (Image image: images) {
			product.addImage(image);
		}
		productRepository.save(product);
		return product;
	}

	@Override
	public boolean removeImage(Product product, Long imageId) {
		Image image = new Image();
		image.setId(imageId);
		System.out.println(product.getImages());
		System.out.println(image);
		boolean exists = product.getImages().remove(image);
		if (!exists){
			System.out.println("Not exists");
			return false;
		} else {
			imageRepository.delete(imageId);
			productRepository.save(product);
			return true;
		}
	}
	
	

}
