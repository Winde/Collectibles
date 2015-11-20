package model.persistence.queues;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import redis.clients.jedis.Jedis;


@Service
public class RedisConnectionManager {	

	private String ip = null;
	private Integer port = null;
	private String password = null;
	private String prefix = null;
	private String databaseNumber = null;
	
	@Autowired
	public RedisConnectionManager(
			@Value("${redis.ip}") String ip, 
			@Value("${redis.port}") Integer port, 
			@Value("${redis.database.number}") String databaseNumber,
			@Value("${redis.password}") String password, 
			@Value("${redis.prefix}") String prefix){
		this.ip = ip;
		this.port = port;
		this.password = password;
		this.prefix = prefix;
		this.databaseNumber  = databaseNumber;
	}
	
	public RedisConnection connect(){	
		
		Jedis jedis = new Jedis(ip,port);		
		jedis.auth(password);		
		//jedis.get("pepito");

		RedisConnection connection = new RedisConnection();
		connection.setClient(jedis);
		return connection;
		
	}
	
}
