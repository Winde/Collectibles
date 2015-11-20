package model.connection;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;

import javax.transaction.Transactional;

import model.dataobjects.HierarchyNode;
import model.dataobjects.Product;
import model.dataobjects.User;
import model.persistence.AuthorRepository;
import model.persistence.ImageRepository;
import model.persistence.ProductRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

@Service
public class BackgroundProcessOwnItems{

	private static final Logger logger = LoggerFactory.getLogger(BackgroundProcessOwnItems.class);
		
	private static final Integer SLEEP_IF_WE_DETECT_TOO_FAST = 2500;
/*	
	private ProductRepository productRepository = null;
	private ImageRepository imageRepository = null;
	private AuthorRepository authorRepository = null;
	private ProductInfoConnector connector = null;
	private String userId = null;
	private List<String> references = null;
	private User user = null;
	private HierarchyNode hierarchyNode;
*/
	
	
	public BackgroundProcessOwnItems(){
		/*
		this.user = user;
		this.hierarchyNode = node;
		this.userId = userId;
		this.productRepository = productRepository;
		this.imageRepository = imageRepository;
		this.authorRepository = authorRepository;
		this.connector = connector;
		*/		
	}
		
	@Transactional 
	protected boolean  doOne(String reference,User user, HierarchyNode hierarchyNode, ProductRepository productRepository, ProductInfoConnector connector) throws TooFastConnectionException{		
		boolean updated = false;
		logger.debug("Processing: " + reference);
		Collection<Product> products = productRepository.findByConnectorReference(connector.getIdentifier(), reference);
		if (products!=null && products.size()>0){
			for (Product product: products){
				product.getOwners().add(user);
			}
			productRepository.save(products);
		} else {		
			Product product = new Product();
			Map<String,String> referenceMap = new HashMap<>();
			Set<User> owners = new HashSet<>();
			owners.add(user);
			referenceMap.put(connector.getIdentifier(), reference);
			product.setConnectorReferences(referenceMap);
			product.setHierarchyPlacement(hierarchyNode);
			product.setOwners(owners);
			product = productRepository.save(product);
			product.updateWithConnector(connector);			
			if (product.getName()==null || "".equals(product.getName().trim())){
				productRepository.delete(product);
			} else {
				updated = true;
			}
		}
		return updated;
	}


	private void additionalSleepIfWeDetectTooFast(){
		if (SLEEP_IF_WE_DETECT_TOO_FAST!=null){
			try {
				Thread.sleep(SLEEP_IF_WE_DETECT_TOO_FAST);
			} catch (InterruptedException e) {
				logger.error("Exception when sleeping after too fast connection", e);
			}
		}
	}
	

	private List<String> getReferencesFromUser(String userId, ProductInfoConnector connector){
		List<String> references = null;
		try {
			references = connector.getOwnedReferences(userId);
		} catch (TooFastConnectionException e) {
			logger.error("Issue when accessing owned games", userId);
		}
		return references; 
	}
	
	@Async
	@Transactional
	public Future<Boolean> run(User user, HierarchyNode node, String userId,ProductRepository productRepository,ImageRepository imageRepository, AuthorRepository authorRepository,  ProductInfoConnector connector){
	
		logger.info("Background Process Started");
		
		List<String> references = null;
		if (userId!=null && connector!=null){
			references  = this.getReferencesFromUser(userId, connector);
		}
				
		if (references!=null && connector!=null && references.size()>0){
		    Iterator<String> iterator = references.iterator();
		    if (iterator!=null){
		    	double i=0;
			    while (iterator.hasNext()){
			    	String reference = iterator.next();    	
				   	
			    	try {
			    		
			    		boolean updated = doOne(reference, user, node, productRepository, connector);
			    		i=i+1;
			    		if (references.size()!=0){
			    			double percentage = (i / new Integer(references.size()).doubleValue())*100.0;			    		
		    				logger.info(connector.getIdentifier()+" percentage completed: " + String.format("%.2f", percentage) + "%");
			    		}
			    		if (updated){
			    			Integer sleep = connector.sleepBetweenCalls();
			    			if (sleep!=null){
			    				Thread.sleep(sleep);
			    			}
		    			} 
		    			
		    			
			    	}catch(TooFastConnectionException ex){
			    		additionalSleepIfWeDetectTooFast();
			    	} catch (InterruptedException e) {
			    		logger.error("Exception when sleeping after too fast connection", e);
					}						
			    }	 
		    }
		}
		logger.info("Background Process Finished");
		return new AsyncResult<>(Boolean.TRUE);
		
	}
	 
}
