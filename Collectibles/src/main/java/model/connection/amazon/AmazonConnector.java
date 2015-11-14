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
