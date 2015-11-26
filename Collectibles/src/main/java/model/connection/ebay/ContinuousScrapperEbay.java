package model.connection.ebay;

import model.connection.ContinuousScrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ContinuousScrapperEbay {

	@Autowired
	private EbayConnector connector;
	
	@Autowired
	private ContinuousScrapper scrapper;
	
	
	@Scheduled(initialDelay=2000, fixedDelay=5000)
	public void doScrape() {
		scrapper.doScrape(connector);
    }
	
}
