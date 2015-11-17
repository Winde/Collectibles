package model.connection;

import java.util.Collection;
import java.util.List;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import model.dataobjects.Product;
import model.persistence.AuthorRepository;
import model.persistence.ImageRepository;
import model.persistence.ProductRepository;

public interface ProductInfoConnector {


	public void processInBackground(Collection<Product> products);

	public void processPricesInBackground(Collection<Product> products);

	public ProductInfoLookupService getImageLookupService();

	@Transactional
	public boolean updateProductTransaction(Product product) throws TooFastConnectionException;
	
	@Transactional
	boolean updatePriceTransaction(Product product) throws TooFastConnectionException;
		
	public boolean updatePrice(Product product)  throws TooFastConnectionException;
	
	public String getIdentifier();
	
	public boolean isApplicable(Product product);
	
	public boolean checkIfAlreadyProcessed(Product product);

	public boolean hasOwnReference();

	public boolean canCreateLinks();

	
}
