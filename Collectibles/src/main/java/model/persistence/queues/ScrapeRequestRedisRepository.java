package model.persistence.queues;

import java.util.Collection;
import java.util.List;

import model.dataobjects.inmemory.ScrapeRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

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
		Jedis jedis = open();
		try {			
			List<String> value = jedis.brpop(POP_TIMEOUT,connectionManager.createUri(QUEUE_PREFIX, connector));			
			
			//logger.debug("POP: " + value);
			
			if (value!=null && value.size()>=2){
				String json = value.get(1);
				request = ScrapeRequest.fromJson(json);
				//if (request!=null && request.getProductId()!=null){
					//Long returnValue = jedis.srem(connectionManager.createUri(SET_PREFIX, connector), request.getProductId().toString());
					//logger.debug("REM FROM SET: " + request.getProductId().toString() +": removed " + returnValue + " values");
				//}
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
			if (request.getProductId()!=null){
				String connector = request.getConnector();
				jedis.srem(connectionManager.createUri(SET_PREFIX, connector), request.getProductId().toString());
				boolean isStillPending = checkPendingWithConnection(request,jedis);
				logger.debug("Removing from set");
				
			}
		} catch(Exception e) {
			logger.error("Exception while Jedis",e);
			executed = false;
		}
		close(jedis);		
		return executed;
	}

	private ScrapeRequest saveWithConnection(ScrapeRequest scrapeReq, Jedis jedis, boolean ignoreCheck){
		String connector = scrapeReq.getConnector();
		String key = null;
		if (scrapeReq!=null && scrapeReq.getProductId()!=null){
			key = scrapeReq.getProductId().toString();
		}
		boolean pending = false;
		if (!ignoreCheck){
			pending = checkPendingWithConnection(scrapeReq,jedis);
			logger.debug("Request: " + scrapeReq + " is pending?" + pending);
		}		
		
		if (!pending){
			String json = ScrapeRequest.toJson(scrapeReq);
			if (json!=null){
				Transaction multi = jedis.multi();				
				if (key!=null){					
					multi.sadd(connectionManager.createUri(SET_PREFIX, connector), key);
				}
				
				if (scrapeReq.isLiveRequest()){		
					multi.rpush(connectionManager.createUri(QUEUE_PREFIX, connector), json);
				} else {					
					multi.lpush(connectionManager.createUri(QUEUE_PREFIX, connector), json);
				}
				
				multi.exec();
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
				this.saveWithConnection(request,jedis, false);
			}
		} catch(Exception e) {
			logger.error("Exception while Jedis",e);
			executed = false;
		}
		close(jedis);
		return executed;
	}
	

	
	private boolean save(ScrapeRequest scrapeReq, boolean ignoreCheck) {		
		Jedis jedis = open();
		boolean executed = true;
		try{
			scrapeReq = saveWithConnection(scrapeReq, jedis, true);
		} catch(Exception e) {
			logger.error("Exception while Jedis",e);
			executed = false;
		}
		close(jedis);
		return executed;
	}
	
	@Override
	public boolean save(ScrapeRequest scrapeReq) {
		return save(scrapeReq,false);
	}
	
	@Override
	public boolean saveIgnoreCheck(ScrapeRequest scrapeReq) {
		return save(scrapeReq,true);
	}
	
	private boolean checkPendingWithConnection(ScrapeRequest request, Jedis jedis){
		boolean pending = false;
		String json = ScrapeRequest.toJson(request);
		if (json!=null){
			String connector = request.getConnector();
			Boolean isMember = jedis.sismember(connectionManager.createUri(SET_PREFIX, connector), request.getProductId().toString());
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
