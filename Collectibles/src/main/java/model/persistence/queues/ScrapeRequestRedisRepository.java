package model.persistence.queues;

import java.util.Collection;
import java.util.List;

import model.dataobjects.inmemory.ScrapeRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import redis.clients.jedis.Jedis;

@Component
public class ScrapeRequestRedisRepository implements ScrapeRequestRepository {

	private final String QUEUE_PREFIX = "queue_";
	private final String SET_PREFIX = "set_";
	
	private static final int POP_TIMEOUT = 10;
	private static final Logger logger = LoggerFactory.getLogger(ScrapeRequestRedisRepository.class);

	
	@Autowired
	private RedisConnectionManager connectionManager;
	
	private Jedis open(){
		return connectionManager.connect().getClient();
	}
	private void close(Jedis jedis){
		jedis.close();
	}

	@Override
	public ScrapeRequest findOldestByConnector(String connector) {
		ScrapeRequest request = null;	
		boolean executed = true;
		Jedis jedis = open();
		try {
			
			List<String> value = jedis.brpop(POP_TIMEOUT,QUEUE_PREFIX + connector);
			
			if (value!=null && value.size()>=2){
				String json = value.get(1);
				request = ScrapeRequest.fromJson(json);											
			}
		} catch(Exception e) {
			logger.error("Exception while Jedis",e);			
		}
		close(jedis);		
		return request;
	}
	
	public boolean markAsCompleted(ScrapeRequest request) {
		Jedis jedis = open();
		boolean executed = true;
		try {
			
			jedis.srem(SET_PREFIX +request.getConnector(), request.getProductId().toString());
		} catch(Exception e) {
			logger.error("Exception while Jedis",e);
			executed = false;
		}
		close(jedis);		
		return executed;
	}

	private ScrapeRequest saveWithConnection(ScrapeRequest scrapeReq, Jedis jedis){			
		boolean pending = checkPendingWithConnection(scrapeReq,jedis);
		if (!pending){
			String json = ScrapeRequest.toJson(scrapeReq);
			if (json!=null){
				if (scrapeReq.isLiveRequest()){		
					jedis.rpush(QUEUE_PREFIX + scrapeReq.getConnector(), json);
				} else {
					jedis.lpush(QUEUE_PREFIX + scrapeReq.getConnector(), json);
				}
				jedis.sadd(SET_PREFIX + scrapeReq.getConnector(), scrapeReq.getProductId().toString());
			}
		}
		return scrapeReq;
	}
	
	@Override
	public boolean save(Iterable<ScrapeRequest> requests) {
		Jedis jedis = open();
		boolean executed = true;
		try{
			for (ScrapeRequest request : requests){
				this.saveWithConnection(request,jedis);
			}
		} catch(Exception e) {
			logger.error("Exception while Jedis",e);
			executed = false;
		}
		close(jedis);
		return executed;
	}
	
	@Override
	public boolean save(ScrapeRequest scrapeReq) {		
		Jedis jedis = open();
		boolean executed = true;
		try{
			scrapeReq = saveWithConnection(scrapeReq, jedis);
		} catch(Exception e) {
			logger.error("Exception while Jedis",e);
			executed = false;
		}
		close(jedis);
		return executed;
	}

	private boolean checkPendingWithConnection(ScrapeRequest request, Jedis jedis){
		boolean pending = false;
		String json = ScrapeRequest.toJson(request);
		if (json!=null){
			Boolean isMember = jedis.sismember(SET_PREFIX + request.getConnector(), request.getProductId().toString());
			if (isMember!=null){
				pending = isMember;
			}			
		}
		return pending;		
	}
	
	@Override
	public Boolean checkPending(ScrapeRequest request) {
		Jedis jedis = open();
		Boolean pending = null; 
		try {
			pending = checkPendingWithConnection(request, jedis);
		} catch(Exception e) {
			logger.error("Exception while Jedis",e);			
		}
		close(jedis);
		return pending;
	}
	
	@Override
	public Boolean checkPending(Collection<ScrapeRequest> requests) {
		Jedis jedis = open();
		Boolean pending = false;
		try {
			for (ScrapeRequest request: requests){
				pending = checkPendingWithConnection(request, jedis);
				if (pending){
					break;
				}
			}
		} catch(Exception e) {
			logger.error("Exception while Jedis",e);
			pending = null;
		}
		
		close(jedis);		
		return pending;
	}
	
	

	
}
