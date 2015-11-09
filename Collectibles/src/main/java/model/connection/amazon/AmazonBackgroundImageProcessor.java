package model.connection.amazon;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import model.dataobjects.Image;
import model.dataobjects.Product;
import model.persistence.ProductRepository;

public class AmazonBackgroundImageProcessor  extends Thread {

	
	private Collection<Product> products = null;
	private ProductRepository productRepository = null;
	private AmazonConnector connector = null;
	
	public AmazonBackgroundImageProcessor(Collection<Product> products,ProductRepository productRepository, AmazonConnector connector){
		this.products = products;
		this.productRepository = productRepository;
		this.connector = connector;
		
	}
		
	public void run(){
				
		System.out.println("Background Process Started");
	    Iterator<Product> iterator = products.iterator();
	    while (iterator.hasNext()){
	    	Product product = iterator.next();
	    	Collection<Image> images = new ArrayList<>();	    	
		   	try {
				connector.updateProductOnlyImage(product, images);				
				productRepository.mergeAndSaveProductWithoutImages(product, images);
				
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			} catch (TooFastConnectionException e) {
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				e.printStackTrace();
			}
	    }	 
	    System.out.println("Background Process Finished");
		
	}
	 
}
