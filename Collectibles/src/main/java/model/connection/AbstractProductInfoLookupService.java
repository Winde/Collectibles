package model.connection;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.imageio.ImageIO;

import model.dataobjects.Product;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractProductInfoLookupService<E> implements ProductInfoLookupService<E> {

	private static final Logger logger = LoggerFactory.getLogger(ProductInfoLookupServiceXML.class);	
	
	private static final int MAXIMUM_DISTANCE_TO_CONSIDER_MULTIPLE_RESULTS = 8;		
	private static final int MAXIMUM_DISTANCE_TO_CONSIDER_SINGLE_RESULTS = 15;
	
	public E fetchDocFromProductForTransient(Product product) throws TooFastConnectionException, FileNotFoundException {
		return this.fetchDocFromProduct(product);
	}
	
	
	protected Date getDateFromYear(Integer year){
		Date date = null;
		if (year!=null){
			Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.DAY_OF_YEAR, 1);
			calendar.set(Calendar.HOUR_OF_DAY, 12);
			calendar.set(Calendar.MINUTE,0);
			calendar.set(Calendar.SECOND,0);
			calendar.set(Calendar.MILLISECOND,0);
			date = calendar.getTime();
		}
		return date;
	}
	
	
	protected int selectName(List<String> productNames, String name){
		int selectedIndex = -1;
		int minDistance = -1;
		for (int i=0;i<productNames.size();i++){
			if (productNames.get(i)!=null && !"".equals(productNames.get(i).trim())){
				
				int distance = StringUtils.getLevenshteinDistance(name, productNames.get(i));							
				
				logger.debug("Title to discriminate: " + productNames.get(i) + ", distance= " + distance);
				
				if (distance < minDistance || minDistance < 0){
					logger.debug("Could be current best");
					logger.debug("Distance < maximum for multiple? " + (distance <= MAXIMUM_DISTANCE_TO_CONSIDER_MULTIPLE_RESULTS));
					logger.debug("Distance < maximum for single? " + (distance <= MAXIMUM_DISTANCE_TO_CONSIDER_SINGLE_RESULTS));
					logger.debug("We have X results =  " + productNames.size());
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
	
	protected byte[] fetchImage(String url) throws TooFastConnectionException {
		byte[] imageInByte = null;
		if (url!=null){
			URL imageURL = null;	
			try {
				imageURL = new URL(url);
			} catch (MalformedURLException e) {				
				logger.error("Malformed URL", e);				
			}
			if (imageURL!=null) {
			    BufferedImage originalImage = null;
				try {
					originalImage = ImageIO.read(imageURL);
				 } catch (IOException ioe) {	    		
		    		if (ioe.getMessage()!=null && ioe.getMessage().contains("HTTP response code: 503")){
		    			throw new TooFastConnectionException();	
		    		} 
		    		logger.error("Exception when obtaining image", ioe);		        	
				}
				if (originalImage!=null){	
					boolean writeError = false;
				    ByteArrayOutputStream baos=new ByteArrayOutputStream();				
					try {
						ImageIO.write(originalImage, "jpg", baos );
					} catch (IOException e) {				
						logger.error("Exception when writing image", e);
						writeError = true;
					}
					if (!writeError){
						imageInByte=baos.toByteArray();
					}
				}
			}
		}
		 
		return imageInByte;
	}
	
	public List<String> getOwnedReferences(String userId){
		return null;
	}
	
	public String getReferenceFromProduct(Product product){
		String reference = null;
		if (product.getConnectorReferences()!=null){
			reference = product.getConnectorReferences().get(this.getIdentifier());
		}
		if (reference==null || "".equals(reference.trim())){
			reference = product.getUniversalReference();
		}
		if (reference!=null && "".equals(reference.trim())){
			reference = null;
		}
		return reference;
	}
}
