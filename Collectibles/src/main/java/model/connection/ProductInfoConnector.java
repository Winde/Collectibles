package model.connection;

import java.util.Collection;

import model.connection.amazon.TooFastConnectionException;
import model.dataobjects.Image;
import model.dataobjects.Product;
import model.persistence.ProductRepository;

public interface ProductInfoConnector {


	public boolean updateProduct(Product product, Collection<Image> images) throws TooFastConnectionException;
			
	public boolean updateProductTransaction(Product product,ProductRepository productRepository) throws TooFastConnectionException;

	public void processInBackground(Collection<Product> products, ProductRepository productRepository);
	
}
