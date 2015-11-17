package model.connection.boardgamegeek;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import model.connection.AbstractProductInfoConnector;
import model.connection.ProductInfoLookupService;
import model.dataobjects.Product;

@Component
public class BoardGameGeekConnector extends AbstractProductInfoConnector{


	@Autowired
	private BoardGameGeekLookupService lookupService;
	
	@Override
	public ProductInfoLookupService getImageLookupService() {
		return lookupService;
	}

	@Override
	public String getIdentifier() {
		return lookupService.getIdentifier();
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
	public String toString(){
		return "BoardGameGeekConnector";
	}

}
