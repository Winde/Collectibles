package model.connection.amazon;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

import javax.imageio.ImageIO;
import javax.transaction.Transactional;

import model.dataobjects.Image;
import model.dataobjects.Product;
import model.persistence.ProductRepository;

import org.springframework.stereotype.Component;

@Component
public class AmazonConnector {

		
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
	
	public boolean updateProduct(Product product, Collection<Image> images) throws TooFastConnectionException{		
		return updateProductDo(product,images); 
	}
		
	@Transactional
	public boolean updateProductTransaction(Product product,ProductRepository productRepository){		
		
		Collection<Image> images = new ArrayList<>();
		
		productRepository.findOne(product.getId());
		boolean updated = false;
		try {
			updated = this.updateProduct(product, images);
		} catch (TooFastConnectionException e) {
			e.printStackTrace();
		}
		productRepository.saveWithImages(product, images);	
		
		return updated;
	}
	
	private boolean updateProductDo(Product product, Collection<Image> imageList) throws TooFastConnectionException{
		boolean modified = false;
		if (product.getAmazonReference()!=null){
			
			if (!Boolean.TRUE.equals(product.getIsAmazonProcessed())
					&&(product.getDescription()==null 
						|| product.getImages()==null
						|| product.getImages().size()==0
						|| product.getAmazonUrl()==null)
					) {
				
				ItemLookup lookup = ItemLookup.getInstance();
				
				String genericAmazonDataUrl = lookup.getMultipleUseUrl(product.getAmazonReference());
				
				if (product.getDescription()==null){
					String amazonDescription = null;
					
					amazonDescription = lookup.parseDescription(genericAmazonDataUrl);
					System.out.println("Obtained Description from Amazon: " + amazonDescription);
					if (amazonDescription!=null){
						product.setDescription(amazonDescription);
						modified = true;
					}				
				}
				
				if (product.getAmazonUrl()==null){
					String amazonUrl = null;
					
					amazonUrl = lookup.parseAmazonUrl(genericAmazonDataUrl);
					System.out.println("Obtained Url from Amazon: " + amazonUrl);
						
					if (amazonUrl!=null){
						product.setAmazonUrl(amazonUrl);
						modified = true;
					}
				}
				
				if (product.getImages()==null || product.getImages().size()<=0){				
					byte[] data = null;
					
					String url = lookup.parseImage(genericAmazonDataUrl);
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
				
				product.setIsAmazonProcessed(Boolean.TRUE);
								
			}
			
						
		}
		return modified;
	}

	public void processImagesInBackground(Collection<Product> products, ProductRepository productRepository){
		
		Collection<Product> clonedProducts = new ArrayList<Product>();
		
		clonedProducts.addAll(products);
		AmazonBackgroundImageProcessor thread = new AmazonBackgroundImageProcessor(clonedProducts, productRepository, this);
		thread.start();
	}
}