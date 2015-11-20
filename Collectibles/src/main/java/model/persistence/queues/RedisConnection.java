package model.persistence.queues;

import redis.clients.jedis.Jedis;

public class RedisConnection {

	private Jedis client = null;
		
	public Jedis getClient() {
		return client;
	}

	public void setClient(Jedis client) {
		this.client = client;
	}
	
	
	public void close(){
		client.close();
	}
	
	
}
