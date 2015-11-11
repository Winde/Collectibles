package model.connection;

import java.util.Collection;

import javax.transaction.Transactional;

import model.dataobjects.Product;
import model.persistence.ImageRepository;
import model.persistence.ProductRepository;

public interface ProductInfoConnector {

	@Transactional
	public boolean updateProductTransaction(Product product,ProductRepository productRepository, ImageRepository imageRepository) throws TooFastConnectionException;

	public void processInBackground(Collection<Product> products, ProductRepository productRepository, ImageRepository imageRepository);
	
	public ProductInfoLookupService getImageLookupService();
}
