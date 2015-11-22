package model.connection;

import javax.transaction.Transactional;

import model.dataobjects.inmemory.ScrapeRequest;

public interface ContinuousScrapperInterface {

	
	@Transactional
	public boolean scrapeOne(ScrapeRequest scrapeReq, ProductInfoConnector connector, String identifier);

	public void doScrape(ProductInfoConnector connector);
	
}
