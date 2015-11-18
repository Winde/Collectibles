package model.dataobjects.events;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManagerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
 
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.internal.SessionFactoryImpl;
import org.hibernate.jpa.HibernateEntityManagerFactory;

@Component
public class HibernateListenersConfigurer {
 
    @Autowired
    private EntityManagerFactory entityManagerFactory;
    @Autowired
    private ProductSaveListener listener;
 
	@PostConstruct
	public void registerListeners() {
	    HibernateEntityManagerFactory hibernateEntityManagerFactory = (HibernateEntityManagerFactory) this.entityManagerFactory;
	    SessionFactoryImpl sessionFactoryImpl = (SessionFactoryImpl) hibernateEntityManagerFactory.getSessionFactory();
	    EventListenerRegistry registry = sessionFactoryImpl.getServiceRegistry().getService(EventListenerRegistry.class);
	    registry.getEventListenerGroup(EventType.PRE_INSERT).appendListener(listener);
	    registry.getEventListenerGroup(EventType.PRE_UPDATE).appendListener(listener);
	}
}