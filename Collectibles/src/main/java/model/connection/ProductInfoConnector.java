package model.connection;

import java.util.List;

import model.dataobjects.Product;
import model.dataobjects.serializable.SerializableHierarchyNode.HierarchyTreeView;

import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.annotation.JsonView;

public interface ProductInfoConnector extends Comparable<ProductInfoConnector>{

	public ProductInfoLookupService getProductInfoLookupService();

	@Transactional
	public boolean updateProductTransaction(Long productId) throws TooFastConnectionException;
	
	@Transactional
	public boolean updateProductWithoutSave(Long productId) throws TooFastConnectionException;
	
	@Transactional
	boolean updateTransitionalTransaction(Long productId) throws TooFastConnectionException;

	public List<String> getOwnedReferences(String userId) throws TooFastConnectionException;
	
	@JsonView(HierarchyTreeView.class)
	public String getIdentifier();
	
	public boolean isApplicable(Product product);
	
	public boolean checkIfAlreadyProcessed(Product product);

	public boolean hasOwnReference();

	public boolean canCreateLinks();

	public boolean supportsTransientData();
	
	@JsonView(HierarchyTreeView.class)
	public boolean supportsPrices();
	
	public boolean supportsRating();
	
	public boolean supportsImportingProducts();
	
	public Integer sleepBetweenCalls();

	public List<String> getMultipleReferences(String criteria) throws TooFastConnectionException;

	public boolean guaranteeUnivocalResponse(Product product);


}
