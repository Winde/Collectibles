package model.dataobjects.serializable;

import java.util.Collection;
import java.util.TreeSet;

import model.connection.ProductInfoConnector;

public class ConnectorInfo implements Comparable<ConnectorInfo>{

	private String name = null;
	private Boolean hasReference = null;
	private Boolean canCreateLinks = null;

	
	
	public static Collection<ConnectorInfo> createConnectorInfo(Collection<ProductInfoConnector> connectors) {
		Collection<ConnectorInfo> result = new TreeSet<>();
		if (connectors!=null){
			for(ProductInfoConnector connector: connectors){
				result.add(new ConnectorInfo(connector));
			}
		}
		return result;
	}
	
	public ConnectorInfo(){
		
	}
	
	public ConnectorInfo(ProductInfoConnector connector){
		this.name = connector.getIdentifier();
		this.hasReference = connector.hasOwnReference();
		this.canCreateLinks = connector.canCreateLinks();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Boolean getHasReference() {
		return hasReference;
	}

	public void setHasReference(Boolean hasReference) {
		this.hasReference = hasReference;
	}

	public Boolean getCanCreateLinks() {
		return canCreateLinks;
	}

	public void setCanCreateLinks(Boolean canCreateLinks) {
		this.canCreateLinks = canCreateLinks;
	}

	@Override
	public int compareTo(ConnectorInfo o) {
		if (this.getName()==null || o==null || o.getName()==null) {
			return -1;
		}
		return this.getName().compareTo(o.getName());
	}
	
}
