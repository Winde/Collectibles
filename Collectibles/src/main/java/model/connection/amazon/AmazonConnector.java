package model.connection.amazon;

import model.connection.AbstractProductInfoConnector;
import model.connection.ProductInfoLookupService;
import model.dataobjects.Product;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AmazonConnector extends AbstractProductInfoConnector {

	private static final int SLEEP_BETWEEN_CALLS = 1400;
	
	@Autowired
	private AmazonItemLookupService itemLookup;
	
	public ProductInfoLookupService getImageLookupService(){
		return itemLookup;
	}

	@Override
	public String getIdentifier() {
		return itemLookup.getIdentifier();
	}

	public String toString(){
		return "AmazonConnector";
	}

	@Override
	public boolean isApplicable(Product product) {
		return product!=null && (
				product.getUniversalReference()!=null 
				|| (product.getConnectorReferences()!=null && product.getConnectorReferences().get(this.getIdentifier())!=null)
				|| product.getName()!=null
		);
	}

	@Override
	public boolean hasOwnReference() {
		return true;
	}

	@Override
	public boolean canCreateLinks() {
		return true;
	}

	@Override
	public Integer sleepBetweenCalls() {
		return SLEEP_BETWEEN_CALLS;
	}

}
