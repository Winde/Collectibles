package model.connection.amazon;

import java.util.Collection;
import java.util.Iterator;

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
		   	
	    	try {
	    		connector.updateProductTransaction(product, productRepository);
	    		Thread.sleep(1400);
	    	}catch(TooFastConnectionException ex){
	    		try {
					Thread.sleep(2500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    	} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}						
	    }	 
	    System.out.println("Background Process Finished");
		
	}
	 
}
