package model.connection.amazon.es;

import model.connection.ContinuousScrapper;
import model.persistence.queues.RedisConnection;
import model.persistence.queues.RedisConnectionManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import redis.clients.jedis.Jedis;

@Component
public class ContinuousScrapperAmazonES{

	private static final Logger logger = LoggerFactory.getLogger(ContinuousScrapperAmazonES.class);	
	
	
	@Autowired
	private AmazonESConnector connector;
	
	@Autowired
	private ContinuousScrapper scrapper;
	
	@Autowired
	private RedisConnectionManager manager;
	
	@Scheduled(initialDelay=2000, fixedDelay=5000)
	public void doScrape() {
		scrapper.doScrape(connector);
    }
	
}
