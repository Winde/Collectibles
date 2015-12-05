package model.connection.amazon.es;

import model.connection.ProductInfoLookupService;
import model.connection.amazon.AbstractAmazonConnector;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AmazonESConnector extends AbstractAmazonConnector{

	@Autowired
	private AmazonESItemLookupService itemLookup;
	

	@Override
	public ProductInfoLookupService getProductInfoLookupService() {
		return itemLookup;
	}

}
