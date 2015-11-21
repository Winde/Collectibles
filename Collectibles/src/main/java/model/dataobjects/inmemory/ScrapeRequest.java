package model.dataobjects.inmemory;

import java.io.IOException;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import model.dataobjects.SimpleIdDao;
import model.persistence.queues.ScrapeRequestRedisRepository;

//@Entity(name="ScrapeRequest")
public class ScrapeRequest {// extends SimpleIdDao{

	private static final Logger logger = LoggerFactory.getLogger(ScrapeRequest.class);

	
	//@Column(name="product_name")
	private String productName;
	
	//@Column(name="product_reference")
	private String productReference;
	
	//@Column(name="product_id")	
	private Long productId;
	
	//@Column(name="connector")
	private String connector;

	//@Column(name="update_transient")
	private boolean onlyTransient = false;
	
	//@Column(name="request_time")
	private Date requestTime;
	
	//@Column(name="live_request")
	private boolean liveRequest;
	
	//@Column(name="attempts")
	private long attempts = 0;
	
	//@Column(name="userId")
	private String userId;
	
	public ScrapeRequest(){}
	
	public String getProductName() {
		return productName;
	}



	public void setProductName(String productName) {
		this.productName = productName;
	}



	public String getProductReference() {
		return productReference;
	}



	public void setProductReference(String productReference) {
		this.productReference = productReference;
	}



	public Long getProductId() {
		return productId;
	}



	public void setProductId(Long productId) {
		this.productId = productId;
	}



	public String getConnector() {
		return connector;
	}



	public void setConnector(String connector) {
		this.connector = connector;
	}



	public boolean isOnlyTransient() {
		return onlyTransient;
	}



	public void setOnlyTransient(boolean onlyTransient) {
		this.onlyTransient = onlyTransient;
	}



	public Date getRequestTime() {
		return requestTime;
	}



	public void setRequestTime(Date requestTime) {
		this.requestTime = requestTime;
	}



	public boolean isLiveRequest() {
		return liveRequest;
	}



	public void setLiveRequest(boolean liveRequest) {
		this.liveRequest = liveRequest;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public long getAttempts() {
		return attempts;
	}

	public void setAttempts(long attempts) {
		this.attempts = attempts;
	}

	public static ScrapeRequest fromJson(String json){
		ObjectMapper mapper = new ObjectMapper();
		ScrapeRequest value = null;
		try {
			value = mapper.readValue(json, ScrapeRequest.class);
		} catch (IOException e) {
			logger.error("Deserializing error from JSON",e);
		}
		return value;
	}
	
	public static String toJson(ScrapeRequest request){
		ObjectMapper mapper = new ObjectMapper();
		String json = null;
		try {
			json = mapper.writeValueAsString(request);
		} catch (JsonProcessingException e) {
			logger.error("Serializing error to JSON",e);
		}
		return json;
	}
	
	public String toString(){
		return "{"+ this.getProductId() + " - " +  this.getConnector() + "}";
	}
}
