package model.connection.drivethrurpg;

import model.connection.AbstractProductInfoConnector;
import model.connection.ProductInfoLookupService;
import model.dataobjects.Product;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DrivethrurpgConnector extends AbstractProductInfoConnector {

	private static final String IDENTIFIER = "DrivethruRPG";

	@Autowired
	private DrivethrurpgItemLookupService itemLookup;
	
	public ProductInfoLookupService getImageLookupService(){
		return itemLookup;
	}
	
	@Override
	public String getIdentifier() { 
		return IDENTIFIER;
	}
	

	public String toString(){
		return "GoodReadsConnector";
	}

	@Override
	public boolean isApplicable(Product product) {
		return product!=null && product.getName()!=null;
	}

	@Override
	public boolean hasOwnReference() {
		return false;
	}
	
}
