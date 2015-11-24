package model.connection.drivethrurpg;

import java.util.List;
import java.util.SortedMap;

import model.connection.AbstractProductInfoConnector;
import model.connection.ProductInfoLookupService;
import model.connection.TooFastConnectionException;
import model.dataobjects.Product;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DrivethrurpgConnector extends AbstractProductInfoConnector {

	private static final int SLEEP_BETWEEN_CALLS = 1400;	
	
	@Autowired
	private DrivethrurpgItemLookupService itemLookup;
	
	public ProductInfoLookupService getProductInfoLookupService(){
		return itemLookup;
	}


	public String toString(){
		return "DrivethruRPGConnector";
	}

	public boolean isApplicable(Product product) {
		String link = null;
		String name = null;
		if (product!=null){
			if (product.getExternalLinks()!=null){
				link = product.getExternalLinks().get(this.getIdentifier());
			}
			name = product.getName();
		}
		
		return ((name!=null && !"".equals(name.trim())) || (link!=null && !"".equals(link.trim())));	
	}

	@Override
	public boolean hasOwnReference() {
		return false;
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
		return true;
	}


	@Override
	public boolean supportsRating() {
		return false;
	}
	
	@Override
	public boolean guaranteeUnivocalResponse(Product product){
		boolean canGuarantee = false;
		SortedMap<String, String> links = product.getExternalLinks();
		if (links!=null){
			String link  =links.get(this.getIdentifier());
			canGuarantee = (link!=null && !"".equals(link.trim()));
		}
		return canGuarantee;
	}
}
