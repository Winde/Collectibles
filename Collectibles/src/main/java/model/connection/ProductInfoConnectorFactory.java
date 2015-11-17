package model.connection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import model.connection.amazon.AmazonConnector;
import model.connection.drivethrurpg.DrivethrurpgConnector;
import model.connection.goodreads.GoodReadsConnector;
import model.dataobjects.HierarchyNode;
import model.dataobjects.Product;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProductInfoConnectorFactory {

	@Autowired
	private AmazonConnector amazonConnector;
	
	@Autowired
	private GoodReadsConnector goodreadsConnector;
	
	@Autowired
	private DrivethrurpgConnector drivethrurpgConnector;
	
	private List<String> getConnectorNamesFromMap( Map<String,ProductInfoConnector> connectors){
		List<String> result = new ArrayList<>();
		if (connectors!=null && connectors.values()!=null){
			Iterator<ProductInfoConnector> iterator = connectors.values().iterator();
			while (iterator.hasNext()){
				ProductInfoConnector connector = iterator.next();
				if (connector.hasOwnReference()){
					result.add(connector.getIdentifier());
				}
			}
		}
		return result;
	}
	
	public Map<String,ProductInfoConnector> getConnectors(){
		Map<String,ProductInfoConnector> connectors = new HashMap<>();
		
		connectors.put(amazonConnector.getIdentifier(), amazonConnector);
		connectors.put(goodreadsConnector.getIdentifier(), goodreadsConnector);
		connectors.put(drivethrurpgConnector.getIdentifier(), drivethrurpgConnector);
		
		return connectors;		
	}
	
	public Map<String,ProductInfoConnector> getConnectors(Product product){
		Map<String,ProductInfoConnector> connectors = new HashMap<>();
		
		connectors.put(amazonConnector.getIdentifier(), amazonConnector);
		connectors.put(goodreadsConnector.getIdentifier(), goodreadsConnector);
		connectors.put(drivethrurpgConnector.getIdentifier(), drivethrurpgConnector);
		
		return connectors;		
	}
	
	public Map<String,ProductInfoConnector> getConnectors(HierarchyNode node){
		Map<String,ProductInfoConnector> connectors = new HashMap<>();
		
		connectors.put(amazonConnector.getIdentifier(), amazonConnector);
		connectors.put(goodreadsConnector.getIdentifier(), goodreadsConnector);
		connectors.put(drivethrurpgConnector.getIdentifier(), drivethrurpgConnector);
		
		return connectors;		
	}
	
	public List<String> getConnectorNames(){
		return getConnectorNamesFromMap(this.getConnectors());
	}
	
	public List<String> getConnectorNames(Product product){
		return getConnectorNamesFromMap(this.getConnectors(product));
	}
	
	public List<String> getConnectorNames(HierarchyNode node){
		return getConnectorNamesFromMap(this.getConnectors(node));
	}
}
