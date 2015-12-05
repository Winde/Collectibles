package model.connection.amazon.us;

import model.connection.ProductInfoLookupService;
import model.connection.amazon.AbstractAmazonConnector;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AmazonUSConnector extends AbstractAmazonConnector{

	@Autowired
	private AmazonUSItemLookupService itemLookup;
	

	@Override
	public ProductInfoLookupService getProductInfoLookupService() {
		return itemLookup;
	}

}
