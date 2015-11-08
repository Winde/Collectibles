package model.connection.amazon;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;

import javax.imageio.ImageIO;

import model.dataobjects.Image;
import model.dataobjects.Product;

import org.springframework.stereotype.Component;

@Component
public class AmazonConnector {

	
	private String getAmazonImageUrl(String id) throws TooFastConnectionException{
		return ItemLookup.getInstance().getImage(id);
	}
	
	private String getDescription(String id) throws TooFastConnectionException{
		return ItemLookup.getInstance().getDescription(id);
	}

	private String getAmazonUrl(String id) throws TooFastConnectionException {
		return ItemLookup.getInstance().getAmazonUrl(id);
	}
			
	private byte[] fetchAmazonImage(String url) throws TooFastConnectionException {
				
		if (url!=null){
			URL imageURL;
			try {
				imageURL = new URL(url);
			} catch (MalformedURLException e) {				
				e.printStackTrace();
				return null;
			}
		    BufferedImage originalImage = null;
			try {
				originalImage = ImageIO.read(imageURL);
			 } catch (IOException ioe) {	    		
	    		if (ioe.getMessage()!=null && ioe.getMessage().contains("HTTP response code: 503")){
	    			throw new TooFastConnectionException();	
	    		} 
	    		ioe.printStackTrace();
	        	return null;
			}
			if (originalImage==null){
				return null;
			}
		    ByteArrayOutputStream baos=new ByteArrayOutputStream();				
			try {
				ImageIO.write(originalImage, "jpg", baos );
			} catch (IOException e) {				
				e.printStackTrace();
				return null;
			}
			byte[] imageInByte=baos.toByteArray();			
			return imageInByte;
		}
		 
		return null;
	}
	
	public boolean updateProductOnlyImage(Product product, Collection<Image> imageList) throws TooFastConnectionException{
		return updateProduct(product,imageList,true);		
	}
	
	public boolean updateProduct(Product product, Collection<Image> imageList) throws TooFastConnectionException{
		return updateProduct(product,imageList,false); 
	}
	
	private boolean updateProduct(Product product, Collection<Image> imageList, boolean onlyImage) throws TooFastConnectionException{
		boolean modified = false;
		if (product.getAmazonReference()!=null){
			if (product.getDescription()==null && !onlyImage){
				String amazonDescription = null;
				
				amazonDescription = getDescription(product.getAmazonReference());
				System.out.println("Obtained Description from Amazon: " + amazonDescription);
				if (amazonDescription!=null){
					product.setDescription(amazonDescription);
					modified = true;
				}				
			}
			
			if (product.getAmazonUrl()==null && !onlyImage){
				String amazonUrl = null;
				
				amazonUrl = getAmazonUrl(product.getAmazonReference());
				System.out.println("Obtained Url from Amazon: " + amazonUrl);
					
				if (amazonUrl!=null){
					product.setAmazonUrl(amazonUrl);
					modified = true;
				}
			}
			
			if (product.getImages()==null || product.getImages().size()<=0){				
				byte[] data = null;
				
				String url = this.getAmazonImageUrl(product.getAmazonReference());
				System.out.println("Obtained Image URL from Amazon: " + url);
				data = this.fetchAmazonImage(url);
								
				if(data!=null){				
					Image image = new Image();
					image.setData(data);
					image.setMain(true);					
					product.addImage(image);
					modified = true;
					imageList.add(image);
				}						
			}			
		}
		return modified;
	}
}
