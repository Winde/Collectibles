package model.dataobjects.events;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.persistence.PrePersist;

import org.hibernate.event.spi.PreInsertEvent;
import org.hibernate.event.spi.PreInsertEventListener;
import org.hibernate.event.spi.PreUpdateEvent;
import org.hibernate.event.spi.PreUpdateEventListener;
import org.springframework.stereotype.Component;

import model.dataobjects.Product;

@Component
public class ProductSaveListener implements PreInsertEventListener,PreUpdateEventListener {

	private <E> void cleanMap(Map<E,String> map) {
		Iterator<Entry<E, String>> iterator = map.entrySet().iterator();
		while (iterator.hasNext()){
			Entry<E, String> entry = iterator.next();
			if (entry.getValue()==null || "".equals(entry.getValue().trim())){
				iterator.remove();
			}
		}
	}
	
	@PrePersist
	public void cleanUp(Product product){
		if (product.getConnectorReferences()!=null){
			this.cleanMap(product.getConnectorReferences());	
		}
		if (product.getExternalLinks()!=null){
			this.cleanMap(product.getConnectorReferences());	
		}		
	}

	@Override
	public boolean onPreInsert(PreInsertEvent event) {
		Object entity = event.getEntity();
		if (entity instanceof Product){
			Product product = (Product) entity;
			this.cleanUp(product);
		}
		return false;
	}

	@Override
	public boolean onPreUpdate(PreUpdateEvent event) {		
		Object entity = event.getEntity();
		if (entity instanceof Product){
			Product product = (Product) entity;
			this.cleanUp(product);
		}
		return false;
	}
}
