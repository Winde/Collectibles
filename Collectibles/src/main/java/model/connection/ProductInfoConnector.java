package model.connection;

import java.util.Collection;
import java.util.List;

import model.dataobjects.Product;
import model.dataobjects.Rating;

import org.springframework.transaction.annotation.Transactional;

public interface ProductInfoConnector {

	public ProductInfoLookupService getProductInfoLookupService();

	@Transactional
	public boolean updateProductTransaction(Product product) throws TooFastConnectionException;
	
	@Transactional
	public boolean updateProductWithoutSave(Product product) throws TooFastConnectionException;
	
	@Transactional
	boolean updateTransitionalTransaction(Product product) throws TooFastConnectionException;
	
	public boolean updateTransitional(Product product)  throws TooFastConnectionException;
	
	public List<String> getOwnedReferences(String userId) throws TooFastConnectionException;
			
	public String getIdentifier();
	
	public boolean isApplicable(Product product);
	
	public boolean checkIfAlreadyProcessed(Product product);

	public boolean hasOwnReference();

	public boolean canCreateLinks();

	public boolean supportsTransientData();
	
	public boolean supportsPrices();
	
	public boolean supportsRating();
	
	public boolean supportsImportingProducts();
	
	public Integer sleepBetweenCalls();

	public List<String> getMultipleReferences(String criteria) throws TooFastConnectionException;

	public boolean guaranteeUnivocalResponse(Product product);


}
