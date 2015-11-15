package model.connection;

import java.util.Collection;
import java.util.Iterator;

import model.dataobjects.Product;
import model.persistence.AuthorRepository;
import model.persistence.ImageRepository;
import model.persistence.ProductRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BackgroundProcessor extends Thread{

	private static final Logger logger = LoggerFactory.getLogger(BackgroundProcessor.class);
		
	private ProductRepository productRepository = null;
	private ImageRepository imageRepository = null;
	private AuthorRepository authorRepository = null;
	private ProductInfoConnector connector = null;
	private Collection<Product> products = null;
	
	public BackgroundProcessor(Collection<Product> products,ProductRepository productRepository,ImageRepository imageRepository, AuthorRepository authorRepository,  ProductInfoConnector connector){
		this.products = products;
		this.productRepository = productRepository;
		this.imageRepository = imageRepository;
		this.authorRepository = authorRepository;
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
		
	protected boolean  doOne(Product product) throws TooFastConnectionException{
		return product.updateWithConnector(getConnector());
		//getConnector().updateProductTransaction(product, getProductRepository(), getImageRepository(), getAuthorRepository());				
	}
	
	
	
	private AuthorRepository getAuthorRepository() {
		return this.authorRepository;
	}

	public void run(){
				
		logger.info("Background Process Started");
		Collection<Product> products = getProducts();
		if (products!=null && connector!=null){
		    Iterator<Product> iterator = getProducts().iterator();
		    if (iterator!=null){
		    	double i=0;
			    while (iterator.hasNext()){
			    	Product product = iterator.next();    	
				   	
			    	try {
			    		
			    		boolean updated = doOne(product);
			    		i=i+1;
			    		if (getProducts().size()!=0){
			    			double percentage = (i / new Integer(getProducts().size()).doubleValue())*100.0;			    		
		    				logger.info(getConnector().getIdentifier()+" percentage completed: " + String.format("%.2f", percentage) + "%");
			    		}
			    		if (updated){
		    				Thread.sleep(1400);
		    			} 
		    			
		    			
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
		logger.info("Background Process Finished");
		
	}
	 
}
