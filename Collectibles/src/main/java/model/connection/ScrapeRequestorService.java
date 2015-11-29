package model.connection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import model.dataobjects.HierarchyNode;
import model.dataobjects.Product;
import model.dataobjects.inmemory.ScrapeRequest;
import model.persistence.ProductRepository;
import model.persistence.queues.ScrapeRequestRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ScrapeRequestorService {


	private static final Logger logger = LoggerFactory.getLogger(ScrapeRequestorService.class);
	
	@Autowired
	private ProductRepository productRepository;
	
	@Autowired
	private ScrapeRequestRepository scrapeRequestRepository;
	
	@Autowired
	private ProductInfoConnectorFactory connectorFactory;
	
	
	
	private ScrapeRequest createScrapeForProduct(String username, Product product,String connector, boolean  liveRequest,boolean onlyTransient){
		ScrapeRequest scrape = new ScrapeRequest();		
		scrape.setProductId(product.getId());
		scrape.setOnlyTransient(onlyTransient);
		scrape.setLiveRequest(liveRequest);
		scrape.setConnector(connector);
		scrape.setUserId(username);
		scrape.setRequestTime(new Date());
		return scrape;
	}
	
	public List<ScrapeRequest> scrapeProduct(String username, Product product, boolean liveRequest, boolean onlyTransient){
		Collection<ProductInfoConnector> connectors = connectorFactory.getConnectors(product,onlyTransient);
		List<ScrapeRequest> requests = new ArrayList<>();
		if (connectors!=null) {
			logger.info("Connectors: " + connectors);
			ScrapeRequest scrape = null;
			for (ProductInfoConnector connector: connectors) {											
				scrape = createScrapeForProduct(username, product, connector.getIdentifier(), liveRequest, onlyTransient);
				scrapeRequestRepository.save(scrape);
				requests.add(scrape);
			}									
		}
		
		return requests;		
	}
	
	//@Scheduled(cron="0 2 * * *")
	@Scheduled(cron="0 0 2 * * *")	
	@Transactional(propagation=Propagation.REQUIRES_NEW)
	public void requestAllPrices(){
		List<Product> products = productRepository.findAll();			
		this.request(products, "scheduled task", false,true);
	}
	
	public void request(Collection<Product> products, String username, boolean liveRequest,boolean onlyTransient){
		
		Collection<ScrapeRequest> scrapeRequests = new ArrayList<>();
		Map<HierarchyNode,List<String>> mapHierarchyConnectors = new HashMap<>();
		for (Product product : products){
			HierarchyNode hierarchy = product.getHierarchyPlacement();
			if (hierarchy!=null){
				List<String> connectorNames = mapHierarchyConnectors.get(hierarchy);
				if (connectorNames == null){
					connectorNames = connectorFactory.getConnectorNames(hierarchy);
					if (connectorNames!=null){
						mapHierarchyConnectors.put(hierarchy, connectorNames);	
					}
				}
				for (String identifier : connectorNames) {
					ScrapeRequest scrape = createScrapeForProduct(username, product, identifier, liveRequest, onlyTransient);
					scrapeRequests.add(scrape);
				}
			}
		}			
		scrapeRequestRepository.save(scrapeRequests);
	}
}
