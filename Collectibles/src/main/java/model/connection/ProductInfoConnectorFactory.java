package model.connection;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.connection.amazon.AmazonConnector;
import model.connection.goodreads.GoodReadsConnector;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProductInfoConnectorFactory {

	@Autowired
	private AmazonConnector amazonConnector;
	
	@Autowired
	private GoodReadsConnector goodreadsConnector;
	
	public Map<String,ProductInfoConnector> getConnectors(){
		Map<String,ProductInfoConnector> connectors = new HashMap<>();
		
		connectors.put(amazonConnector.getIdentifier(), amazonConnector);
		connectors.put(goodreadsConnector.getIdentifier(), goodreadsConnector);
		
		return connectors;
		
	}
	
	
}
