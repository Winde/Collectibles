package model.connection.amazon.es;

import model.connection.amazon.AbstractAmazonItemLookupService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AmazonESItemLookupService extends AbstractAmazonItemLookupService{

	private String ENDPOINT = null;
	
	private final String IDENTIFIER = "Amazon ES";
	
	
	@Autowired
	public AmazonESItemLookupService(
			@Value("${AMAZON.ENDPOINT.ES}") String ENDPOINT){
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
