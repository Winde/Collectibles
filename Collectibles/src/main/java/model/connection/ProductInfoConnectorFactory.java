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
				result.add(connector.getIdentifier());				
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
	
	public ProductInfoConnector getConnector(String connectorName) {
		if (connectorName!=null && connectors!=null && connectors.size()>0){
			for (ProductInfoConnector connector: connectors){
				if (connectorName.equals(connector.getIdentifier())){
					return connector;
				}
			}
		}
		return null;
	}
	
	private void filterSupportTransient(Collection<ProductInfoConnector> collection){		
		if (collection!=null){
			Iterator<ProductInfoConnector> iterator = collection.iterator();
			while (iterator.hasNext()){
				ProductInfoConnector connector = iterator.next();
				if (!connector.supportsTransientData()){
					iterator.remove();
				}
			}
		}		
	}
	
	public Collection<ProductInfoConnector> getConnectors(boolean requireSupportsTransient) {
		Collection<ProductInfoConnector> allConnectors= this.getConnectors();
		if (requireSupportsTransient){
			filterSupportTransient(allConnectors);
		}
		return null;
	}

	
	public Collection<ProductInfoConnector> getConnectors(Product product, boolean requireSupportsTransient){
		Collection<ProductInfoConnector> connectorsForProduct = this.getConnectors(product.getHierarchyPlacement());
		if (requireSupportsTransient){
			filterSupportTransient(connectorsForProduct);
		}
		return connectorsForProduct;
	}
	
	public Collection<ProductInfoConnector> getConnectors(HierarchyNode node, boolean requireSupportsTransient){
		Collection<ProductInfoConnector> connectorsForProduct = this.getConnectors(node);
		if (requireSupportsTransient){
			filterSupportTransient(connectorsForProduct);
		}
		return connectorsForProduct;
	}
	
	public Collection<ProductInfoConnector> getConnectors(Product product){
		return this.getConnectors(product.getHierarchyPlacement());		
	}
	
	public Collection<ProductInfoConnector> getConnectors(HierarchyNode node){
		Set<String> connectorIdentifiers = new HashSet<>();
		HierarchyNode current = node;
		connectorIdentifiers.addAll(current.getConnectorNames());
		while (current.getFather()!=null){
			logger.info("Parsing connectors for: " + current.getName());
			current = current.getFather();
			connectorIdentifiers.addAll(current.getConnectorNames());
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
