package model.connection;

import java.util.Collection;
import java.util.List;

import model.dataobjects.Product;

import org.springframework.transaction.annotation.Transactional;

public interface ProductInfoConnector {

	public ProductInfoLookupService getImageLookupService();

	@Transactional
	public boolean updateProductTransaction(Product product) throws TooFastConnectionException;
	
	@Transactional
	public boolean updateProductWithoutSave(Product product) throws TooFastConnectionException;
	
	@Transactional
	boolean updateSuperficialTransaction(Product product) throws TooFastConnectionException;
	
	public boolean updateSuperficial(Product product)  throws TooFastConnectionException;
	
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

}
