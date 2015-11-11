package model.connection;

import java.util.Collection;
import java.util.Iterator;

import javax.transaction.Transactional;

import model.connection.amazon.AmazonConnector;
import model.dataobjects.Product;
import model.persistence.ImageRepository;
import model.persistence.ProductRepository;

public class BackgroundProcessor extends Thread {

	private ProductRepository productRepository = null;
	private ImageRepository imageRepository = null;
	private ProductInfoConnector connector = null;
	private Collection<Product> products = null;
	
	public BackgroundProcessor(Collection<Product> products,ProductRepository productRepository,ImageRepository imageRepository,  ProductInfoConnector connector){
		this.products = products;
		this.productRepository = productRepository;
		this.imageRepository = imageRepository;
		this.connector = connector;		
	}
	
	
	public ProductRepository getProductRepository() {
		return productRepository;
	}

	public ImageRepository getImageRepository() {
		return imageRepository;
	}

	public ProductInfoConnector getConnector() {
		return connector;
	}

	protected Collection<Product> getProducts() {
		return this.products;
	}
		
	protected void doOne(Product product) throws TooFastConnectionException{
		getConnector().updateProductTransaction(product, getProductRepository(), getImageRepository());				
	}
	
	
	
	public void run(){
				
		System.out.println("Background Process Started");
		Collection<Product> products = getProducts();
		if (products!=null){
		    Iterator<Product> iterator = getProducts().iterator();
		    if (iterator!=null){
			    while (iterator.hasNext()){
			    	Product product = iterator.next();    	
				   	
			    	try {
			    		doOne(product);
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
		    }
		}
	    System.out.println("Background Process Finished");
		
	}
	 
}
