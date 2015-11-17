package model.connection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import model.connection.amazon.AmazonConnector;
import model.connection.boardgamegeek.BoardGameGeekConnector;
import model.connection.boardgamegeek.BoardGameGeekLookupService;
import model.connection.drivethrurpg.DrivethrurpgConnector;
import model.connection.goodreads.GoodReadsConnector;
import model.dataobjects.HierarchyNode;
import model.dataobjects.Product;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProductInfoConnectorFactory {

	private static final Logger logger = LoggerFactory.getLogger(Product.class);	
	
	@Autowired
	private Collection<ProductInfoConnector> connectors;
	
	private List<String> getConnectorNames( Collection<ProductInfoConnector> connectors){
		List<String> result = new ArrayList<>();
		if (connectors!=null){
			Iterator<ProductInfoConnector> iterator = connectors.iterator();
			while (iterator.hasNext()){
				ProductInfoConnector connector = iterator.next();
				if (connector.hasOwnReference()){
					result.add(connector.getIdentifier());
				}
			}
		}
		return result;
	}
	
	public Collection<ProductInfoConnector> getConnectors(){
		Collection<ProductInfoConnector> result = null;
		if (connectors!=null){
			result = new ArrayList(connectors);
		}
		return result;
	}
	
	public Collection<ProductInfoConnector> getConnectors(Product product){
		return this.getConnectors(product.getHierarchyPlacement());		
	}
	
	public Collection<ProductInfoConnector> getConnectors(HierarchyNode node){
		Set<String> connectorIdentifiers = new HashSet<>();
		HierarchyNode current = node;
		connectorIdentifiers.addAll(current.getConnectorsNames());
		while (current.getFather()!=null){
			logger.info("Parsing connectors for: " + current.getName());
			current = current.getFather();
			connectorIdentifiers.addAll(current.getConnectorsNames());
		}
		
				
		Set<ProductInfoConnector> result = new HashSet<>(); 
		for (String identifier : connectorIdentifiers){
			for (ProductInfoConnector connector: connectors) {
				if (identifier.equals(connector.getIdentifier())){
					result.add(connector);
				}
			}		
		}
		return result;
	}
	
	public List<String> getConnectorNames(){
		return getConnectorNames(this.getConnectors());
	}
	
	public List<String> getConnectorNames(Product product){
		return getConnectorNames(this.getConnectors(product));
	}
	
	public List<String> getConnectorNames(HierarchyNode node){
		return getConnectorNames(this.getConnectors(node));
	}
}
