package model.dataobjects.inmemory;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;

import model.dataobjects.SimpleIdDao;

@Entity(name="ScrapeRequest")
public class ScrapeRequest extends SimpleIdDao{

	@Column(name="product_name")
	private String productName;
	
	@Column(name="product_reference")
	private String productReference;
	
	@Column(name="product_id")	
	private Long productId;
	
	@Column(name="connector")
	private String connector;

	@Column(name="update_transient")
	private boolean onlyTransient = false;
	
	@Column(name="request_time")
	private Date requestTime;
	
	@Column(name="live_request")
	private boolean liveRequest;
	
	@Column(name="attempts")
	private long attempts = 0;
	
	@Column(name="userId")
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

	public String toString(){
		return "{"+ this.getId() + " - " +  this.getConnector() + "}";
	}
}
