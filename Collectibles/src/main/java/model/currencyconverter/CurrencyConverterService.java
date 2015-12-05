package model.currencyconverter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CurrencyConverterService {

	private static final Logger logger = LoggerFactory.getLogger(CurrencyConverterService.class);	
	
	@Autowired
	private CurrencyAPI api;
		
	public Long convert(String currencySource,String currencyTarget, Long value){		
		Long converted = null;
		Double rate = api.getRate(currencySource, currencyTarget);
		if (rate!=null){
			converted = new Double(value * rate).longValue();
		}
		return converted;
	}
	
}
