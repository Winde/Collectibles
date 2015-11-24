package model.connection.boardgamegeek;

import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import model.connection.AbstractProductInfoConnector;
import model.connection.ProductInfoLookupService;
import model.connection.TooFastConnectionException;
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
	public boolean isApplicable(Product product) {
		String reference = lookupService.getReferenceFromProduct(product);
		String name = product.getName();
		return ((name!=null && !"".equals(name.trim())) || (reference!=null && !"".equals(reference.trim())));	
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

	@Override
	public List<String> getOwnedReferences(String userId)
			throws TooFastConnectionException {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public boolean supportsPrices() { 
		return false;
	}


	@Override
	public boolean supportsRating() {
		return true;
	}

}
