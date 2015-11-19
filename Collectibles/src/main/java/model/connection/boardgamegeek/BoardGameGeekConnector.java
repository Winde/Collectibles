package model.connection.boardgamegeek;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import model.connection.AbstractProductInfoConnector;
import model.connection.ProductInfoLookupService;
import model.dataobjects.Product;

@Component
public class BoardGameGeekConnector extends AbstractProductInfoConnector{

	private static final int SLEEP_BETWEEN_CALLS = 1400;	
	
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
		return product!=null && 
			(product.getUniversalReference()!=null 
				|| (product.getConnectorReferences()!=null && product.getConnectorReferences().get(this.getIdentifier())!=null)
				|| product.getName()!=null
			);
	}

	@Override
	public boolean hasOwnReference() {
		return true;
	}
	
	@Override
	public String toString(){
		return "BoardGameGeekConnector";
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
