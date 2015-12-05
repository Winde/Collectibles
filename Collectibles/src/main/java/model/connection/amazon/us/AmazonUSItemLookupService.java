package model.connection.amazon.us;

import model.connection.amazon.AbstractAmazonItemLookupService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AmazonUSItemLookupService extends AbstractAmazonItemLookupService{

	private String ENDPOINT = null;
	
	private final String IDENTIFIER = "Amazon US";
	
	
	@Autowired
	public AmazonUSItemLookupService(
			@Value("${AMAZON.ENDPOINT.US}") String ENDPOINT){
		this.ENDPOINT = ENDPOINT;
	}


	@Override
	public String getIdentifier(){
		return IDENTIFIER;
	}


	@Override
	public String getENDPOINT() {
		return ENDPOINT;
	}

}
