package model.connection;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.transaction.Transactional;

import model.dataobjects.HierarchyNode;
import model.dataobjects.Product;
import model.dataobjects.User;
import model.dataobjects.inmemory.ScrapeRequest;
import model.persistence.HierarchyRepository;
import model.persistence.ProductRepository;
import model.persistence.UserRepository;
import model.persistence.queues.ScrapeRequestRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ContinuousScrapper implements ContinuousScrapperInterface {

	private static final Logger logger = LoggerFactory.getLogger(ContinuousScrapper.class);	
	
	private static final int MAX_ATTEMPTS_NUMBER = 2;
	
	@Autowired
	private ProductRepository productRepository;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private ScrapeRequestRepository scrapeRequestRepository;

	@Autowired
	private HierarchyRepository hierarchyRepository;
		
		
	
	private void sleepIfTooFast(){
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			logger.error("Detected exception" , e);
		}
	}

	@Transactional
	public boolean scrapeOne(ScrapeRequest scrapeReq, ProductInfoConnector connector, String identifier){
		boolean accessedRemoteConnector = true;
		boolean processed = true;
		boolean markedAsCompleted = false;
		try{
			if (scrapeReq!=null && scrapeReq.getProductId()!=null){										
				try {
					long start = new Date().getTime();
					Product product = productRepository.findOne(scrapeReq.getProductId());
					if (product!=null){
						if (scrapeReq.isOnlyTransient()){					
							connector.updateTransitionalTransaction(product);
						} else{
							connector.updateProductTransaction(product);
						}
					} else {
						logger.error(identifier + " product is null");
					}
					logger.info(identifier + " Execution took : " + (new Date().getTime()-start) + " ms");
				} catch (TooFastConnectionException e) {
					logger.error("Too Fast connection" + e);
					processed = false;
					sleepIfTooFast();
				} catch (Exception e){
					logger.error("Other exception "+ e);
					e.printStackTrace();
					processed = false;
				}
			} else if (scrapeReq!=null && scrapeReq.getProductReference()!=null && scrapeReq.getHierarchy()!=null){
				try {
					HierarchyNode hierarchyNode = hierarchyRepository.findOne(scrapeReq.getHierarchy());
					if (hierarchyNode!=null){
						boolean alreadyExists = false;
						Product product = null;
						Collection<Product> products = productRepository.findByConnectorReference(identifier, scrapeReq.getProductReference());
						if (products!=null && products.size()==1){
							product = products.iterator().next();
							alreadyExists = true;
						}  else {
							Map<String,String> connectorReferences = new HashMap<>();							
							
							connectorReferences.put(identifier,scrapeReq.getProductReference());
							product = new Product();
							product.setConnectorReferences(connectorReferences);							
						}
						if (product.getHierarchyPlacement()==null){
							product.setHierarchyPlacement(hierarchyNode);
						}
						if (scrapeReq.getUserId()!=null){
							User user = userRepository.findOne(scrapeReq.getUserId());
							if (user!=null){
								Set<User> owners = product.getOwners();
								if (owners==null){
									owners = new HashSet<>();
									product.setOwners(owners);
								}
								owners.add(user);
							}
						}
						boolean updated = false;
						if (alreadyExists){
							updated  = true;
							accessedRemoteConnector = false;
						} else {
							updated = connector.updateProductWithoutSave(product);
						}
						if (updated){																
							processed = true;								
							if (product.getName()!=null && !"".equals(product.getName().trim())){
								productRepository.save(product);
								
							} 
						} else {
							processed = false;
						}
					}
					
				} catch (TooFastConnectionException e) {
					logger.error("Too Fast connection" + e);
					processed = false;
					sleepIfTooFast();
				}catch (Exception e){
					logger.error("Other exception "+ e);
					e.printStackTrace();
					processed = false;
				}
			}
			
			if (scrapeReq!=null){
				
				if (processed){	
					scrapeRequestRepository.markAsCompleted(scrapeReq);
					markedAsCompleted = true;
					logger.info("Processed scrapeReq: " + scrapeReq);
				} else {
					logger.info("NOT Processed scrapeReq: " + scrapeReq);
					scrapeReq.setAttempts(scrapeReq.getAttempts()+1);
					scrapeReq.setRequestTime(new Date());
					if (scrapeReq.getAttempts()<=MAX_ATTEMPTS_NUMBER){
						logger.info("Re-injecting to queue: " + scrapeReq);
						scrapeRequestRepository.saveIgnoreCheck(scrapeReq);
						markedAsCompleted = true;
					} else {
						logger.info("Abandoning due to max requests: " + scrapeReq);
					}
				}
				Boolean pending = scrapeRequestRepository.checkPending(scrapeReq);
				logger.info("Finished scraping, still pending? " + pending);
			}
		}catch (Exception ex){
			if (!markedAsCompleted){
				scrapeRequestRepository.markAsCompleted(scrapeReq);
			}
		}
		return accessedRemoteConnector;
	}

	public void doScrape(ProductInfoConnector connector) {
		String identifier = connector.getIdentifier();
		Integer sleep = connector.sleepBetweenCalls();
		while(true){
			
			if (sleep == null || sleep < 200){
				sleep = 1000;
			}
			boolean accessedRemoteConnector = true; 					
			//logger.info(identifier + " Scanning");
			ScrapeRequest scrapeReq = scrapeRequestRepository.findOldestByConnector(identifier);			
			
			
			if (scrapeReq!=null){
				logger.info(identifier + " found " + scrapeReq);
				accessedRemoteConnector = scrapeOne(scrapeReq, connector, identifier);
			}			
			
			try {				
				if (accessedRemoteConnector){
					Thread.sleep(sleep);
				}
			} catch (InterruptedException e) {
				logger.error("Interrupt sleep", e);
			}
		}
    }
	
}
