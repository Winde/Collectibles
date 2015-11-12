package model.connection.amazon;

import java.util.ArrayList;
import java.util.Collection;

import model.connection.AbstractProductInfoConnector;
import model.connection.BackgroundProcessor;
import model.connection.ProductInfoLookupService;
import model.connection.TooFastConnectionException;
import model.dataobjects.Product;
import model.persistence.AuthorRepository;
import model.persistence.ImageRepository;
import model.persistence.ProductRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AmazonConnector extends AbstractProductInfoConnector {

	@Autowired
	private AmazonItemLookupService itemLookup;
	
	public ProductInfoLookupService getImageLookupService(){
		return itemLookup;
	}
	
	


	@Override
	public void processInBackground(Collection<Product> products, ProductRepository productRepository, ImageRepository imageRepository, AuthorRepository authorRepository){
		
		Collection<Product> clonedProducts = new ArrayList<Product>();		
		clonedProducts.addAll(products);
		BackgroundProcessor thread = new BackgroundProcessor(clonedProducts, productRepository, imageRepository, authorRepository, this);
		thread.start();
	}

	@Override
	protected boolean checkIfWeProcess(Product product) {
		return product!=null && (product.getIsAmazonProcessed()==null || !product.getIsAmazonProcessed());
	}

	@Override
	protected void storeAfterSuccess(Product product,
			ProductRepository productRepository) {
		product.setIsAmazonProcessed(Boolean.TRUE);
		productRepository.save(product);		
	}


}
