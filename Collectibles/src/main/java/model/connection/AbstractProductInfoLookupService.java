package model.connection;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractProductInfoLookupService<E> implements ProductInfoLookupService<E> {

	private static final Logger logger = LoggerFactory.getLogger(ProductInfoLookupServiceXML.class);	
	
	private static final int MAXIMUM_DISTANCE_TO_CONSIDER_MULTIPLE_RESULTS = 8;		
	private static final int MAXIMUM_DISTANCE_TO_CONSIDER_SINGLE_RESULTS = 15;
	
	
	
	protected int selectName(List<String> productNames, String name){
		int selectedIndex = -1;
		int minDistance = -1;
		for (int i=0;i<productNames.size();i++){
			if (productNames.get(i)!=null && !"".equals(productNames.get(i).trim())){
				
				int distance = StringUtils.getLevenshteinDistance(name, productNames.get(i));							
				
				logger.info("Title to discriminate: " + productNames.get(i) + ", distance= " + distance);
				
				if (distance < minDistance || minDistance < 0){
					logger.info("Could be current best");
					logger.info("Distance < maximum for multiple? " + (distance <= MAXIMUM_DISTANCE_TO_CONSIDER_MULTIPLE_RESULTS));
					logger.info("Distance < maximum for single? " + (distance <= MAXIMUM_DISTANCE_TO_CONSIDER_SINGLE_RESULTS));
					logger.info("We have X results =  " + productNames.size());
					if (
						(distance <= MAXIMUM_DISTANCE_TO_CONSIDER_MULTIPLE_RESULTS)
						||
						(distance <= MAXIMUM_DISTANCE_TO_CONSIDER_SINGLE_RESULTS && productNames.size()==1)
					){
					minDistance = distance;
					selectedIndex = i;
					}																																
				}						
			}					
		}
		if (selectedIndex>=0){
			logger.info("Selected "+productNames.get(selectedIndex)+" with distance ="+minDistance);
		}
		return selectedIndex;
	}
}
