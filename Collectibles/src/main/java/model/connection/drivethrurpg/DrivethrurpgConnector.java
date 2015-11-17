package model.connection.drivethrurpg;

import model.connection.AbstractProductInfoConnector;
import model.connection.ProductInfoLookupService;
import model.dataobjects.Product;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DrivethrurpgConnector extends AbstractProductInfoConnector {


	@Autowired
	private DrivethrurpgItemLookupService itemLookup;
	
	public ProductInfoLookupService getImageLookupService(){
		return itemLookup;
	}
	
	@Override
	public String getIdentifier() { 
		return itemLookup.getIdentifier();
	}
	

	public String toString(){
		return "DrivethruRPGConnector";
	}

	@Override
	public boolean isApplicable(Product product) {
		return product!=null && (product.getName()!=null || (product.getExternalLinks()!=null && product.getExternalLinks().get(this.getIdentifier())!=null));
	}

	@Override
	public boolean hasOwnReference() {
		return false;
	}

	@Override
	public boolean canCreateLinks() {		
		return true;
	}
	
}
