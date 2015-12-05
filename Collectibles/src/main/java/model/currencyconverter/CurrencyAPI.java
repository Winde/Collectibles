package model.currencyconverter;

import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class CurrencyAPI {

	private static final Logger logger = LoggerFactory.getLogger(CurrencyAPI.class);
	
	private String generateURL(String currencySource,String currencyTarget){
		String url = "http://api.fixer.io/latest?base="+currencyTarget+"&symbols="+currencySource;
		return url;
	}
	
	@Cacheable(value="currencyRate", cacheManager="currencyRateCacheManager")
	public Double getRate(String currencySource,String currencyTarget){
		logger.info("CALLING CURRENCY API: " + currencySource + " TO " + currencyTarget);
		String url = this.generateURL(currencySource, currencyTarget);
		Double rate = null;
		try {			
			URL urlObject = new URL(url);   
			String json = IOUtils.toString(urlObject);
			if (json!=null){				
				ObjectMapper mapper = new ObjectMapper();					
				JsonNode doc = mapper.readTree(json);
				if (doc!=null){
					doc = doc.path("rates").path(currencySource);
					rate = doc.asDouble();
				}
			}
		} catch (IOException e) {
			logger.error("Error in currency API ", e);
		}
		
		
		return rate;
	}
}
