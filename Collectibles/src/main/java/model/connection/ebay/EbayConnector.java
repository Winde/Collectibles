package model.connection.ebay;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import model.connection.AbstractProductInfoConnector;
import model.connection.ProductInfoLookupService;
import model.connection.TooFastConnectionException;
import model.dataobjects.Product;

@Component
public class EbayConnector extends AbstractProductInfoConnector{

	private static final Integer SLEEP_BETWEEN_CALLS = 1200;
	
	@Autowired
	private EbayInfoLookupService itemLookup;
	
	@Override
	public ProductInfoLookupService getProductInfoLookupService() {
		return itemLookup;
	}

	@Override
	public List<String> getOwnedReferences(String userId)
			throws TooFastConnectionException {
		return null;
	}

	@Override
	public boolean isApplicable(Product product) {
		String reference = itemLookup.getReferenceFromProduct(product);
		return reference!=null && !"".equals(reference.trim());
		
	}

	@Override
	public boolean hasOwnReference() { 
		return true;
	}

	@Override
	public boolean canCreateLinks() {
		return false;
	}

	@Override
	public boolean supportsPrices() {
		return true;
	}

	@Override
	public boolean supportsRating() {
		return false;
	}

	@Override
	public Integer sleepBetweenCalls() {
		return SLEEP_BETWEEN_CALLS;
	}

}
