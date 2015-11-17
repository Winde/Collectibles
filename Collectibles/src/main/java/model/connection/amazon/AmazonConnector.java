package model.connection.amazon;

import model.connection.AbstractProductInfoConnector;
import model.connection.ProductInfoLookupService;
import model.dataobjects.Product;

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
	public String getIdentifier() {
		return itemLookup.getIdentifier();
	}

	public String toString(){
		return "AmazonConnector";
	}

	@Override
	public boolean isApplicable(Product product) {
		return product!=null && (product.getUniversalReference()!=null || (product.getConnectorReferences()!=null && product.getConnectorReferences().containsKey(this.getIdentifier())));
	}

	@Override
	public boolean hasOwnReference() {
		return true;
	}

	@Override
	public boolean canCreateLinks() {
		return true;
	}

}
