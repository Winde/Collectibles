package model.connection;

import java.util.Date;
import java.util.List;

import model.dataobjects.Product;
import model.dataobjects.inmemory.ScrapeRequest;
import model.persistence.ProductRepository;
import model.persistence.ScrapeRequestRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ContinuousScrapper {

	private static final Logger logger = LoggerFactory.getLogger(ContinuousScrapper.class);	
	
	private static final int MAX_ATTEMPTS_NUMBER = 12;
	
	@Autowired
	private ProductRepository productRepository;
	
	@Autowired
	private ScrapeRequestRepository scrapeRequestRepository;
		
		
	
	private void sleepIfTooFast(){
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			logger.error("Detected exception" , e);
		}
	}


	public void doScrape(ProductInfoConnector connector) {
		while(true){
			Integer sleep = connector.sleepBetweenCalls();
			if (sleep == null || sleep < 200){
				sleep = 1000;
			}
			
			ScrapeRequest scrapeReq = null;			
			List<ScrapeRequest> scrapeRequests = scrapeRequestRepository.findOldestByConnector(connector.getIdentifier());
			if (scrapeRequests.size()>0){				
				scrapeReq = scrapeRequests.get(0);
				logger.info(connector.getIdentifier()+" Found scrapeReq: " + scrapeReq);
			}
			boolean processed = true;
			if (scrapeReq!=null){
				
				try {
					long start = new Date().getTime();
					Product product = productRepository.findOne(scrapeReq.getProductId());
					if (product!=null){
						if (scrapeReq.isOnlyTransient()){					
							connector.updatePriceTransaction(product);
						} else{
							connector.updateProductTransaction(product);
						}
					} else {
						logger.error(connector.getIdentifier() + " product is null");
					}
					logger.info(connector.getIdentifier() + " Execution took : " + (new Date().getTime()-start) + " ms");
				} catch (TooFastConnectionException e) {
					logger.error("Too Fast connection" + e);
					processed = false;
					sleepIfTooFast();
				} catch (Exception e){
					logger.error("Other exception "+ e);
					e.printStackTrace();
					processed = false;
				}
			}
			
			if (scrapeReq!=null){
				
				if (processed){			
					scrapeRequestRepository.delete(scrapeReq);
					logger.info("Processed scrapeReq: " + scrapeReq);
				} else {
					scrapeReq.setAttempts(scrapeReq.getAttempts()+1);
					scrapeReq.setRequestTime(new Date());
					if (scrapeReq.getAttempts()>MAX_ATTEMPTS_NUMBER){
						scrapeRequestRepository.delete(scrapeReq);
					} else {
						scrapeRequestRepository.save(scrapeReq);
					}
				}
			}
			try {				
				Thread.sleep(sleep);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
    }
	
}
