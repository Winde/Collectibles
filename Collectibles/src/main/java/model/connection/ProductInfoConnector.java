package model.connection;

import java.util.Collection;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import model.dataobjects.Product;
import model.persistence.AuthorRepository;
import model.persistence.ImageRepository;
import model.persistence.ProductRepository;

public interface ProductInfoConnector {


	public void processInBackground(Collection<Product> products);
	
	public ProductInfoLookupService getImageLookupService();

	@Transactional
	public boolean updateProductTransaction(Product product) throws TooFastConnectionException;

	
}
