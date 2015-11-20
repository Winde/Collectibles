package model.connection;

import java.util.Collection;
import java.util.List;

import model.dataobjects.Product;

import org.springframework.transaction.annotation.Transactional;

public interface ProductInfoConnector {


	public void processInBackground(Collection<Product> products);

	public void processPricesInBackground(Collection<Product> products);

	public ProductInfoLookupService getImageLookupService();

	@Transactional
	public boolean updateProductTransaction(Product product) throws TooFastConnectionException;
	
	boolean updateProductTransaction(Product product, boolean save) throws TooFastConnectionException;
	
	@Transactional
	boolean updatePriceTransaction(Product product) throws TooFastConnectionException;
	
	public boolean updatePrice(Product product)  throws TooFastConnectionException;
	
	public List<String> getOwnedReferences(String userId) throws TooFastConnectionException;
			
	public String getIdentifier();
	
	public boolean isApplicable(Product product);
	
	public boolean checkIfAlreadyProcessed(Product product);

	public boolean hasOwnReference();

	public boolean canCreateLinks();

	public boolean supportsPrices();
	
	public Integer sleepBetweenCalls();

	
	
	
	
}
