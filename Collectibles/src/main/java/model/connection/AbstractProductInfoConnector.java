package model.connection;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import model.connection.amazon.AmazonItemLookupService;
import model.dataobjects.Author;
import model.dataobjects.Image;
import model.dataobjects.Product;
import model.persistence.AuthorRepository;
import model.persistence.ImageRepository;
import model.persistence.ProductRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

public abstract class AbstractProductInfoConnector implements ProductInfoConnector{

	private static final Logger logger = LoggerFactory.getLogger(AbstractProductInfoConnector.class);
	
	@Autowired
	private PlatformTransactionManager transactionManager;
	
	@Autowired
	private ProductRepository productRepository;
	
	@Autowired
	private ImageRepository imageRepository;
	
	@Autowired
	private AuthorRepository authorRepository;
	
	@Override	
	public boolean updatePriceTransaction(Product product) throws TooFastConnectionException{
		TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
		AbstractProductInfoConnector connector = this;
		boolean result = true; 		
		result = transactionTemplate.execute(new TransactionCallback<Boolean>(){

			@Override
			public Boolean doInTransaction(TransactionStatus status) {
				Product productInDb = productRepository.findOne(product.getId());
				boolean updated = false;
				try {
					updated = connector.updatePrice(productInDb);
				} catch (TooFastConnectionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (updated){
					productRepository.save(productInDb);
				}
				return updated;
			}
			
		});
		
		return result;
	}
	
	@Override	
	public boolean updatePrice(Product product) throws TooFastConnectionException{
		TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
		AbstractProductInfoConnector connector = this;
		boolean result = true; 		

		logger.info("Starting access to provider : " + connector.getIdentifier());
		boolean updated = false;
		try {
								
			ProductInfoLookupService imageLookup = connector.getImageLookupService();										
			if (imageLookup!=null){
				Object doc = imageLookup.fetchDocFromProduct(product);
				if (doc!=null){
					Map<String,Long> prices = imageLookup.getDollarPrice(doc);					
					if (prices!=null){
						for (Entry<String,Long> priceEntry: prices.entrySet()){
							String key = connector.getIdentifier();
							if (priceEntry.getKey()!=null && !"".equals(priceEntry.getKey().trim())){
								key = key + " - " + priceEntry.getKey(); 
							}
							product.setDollarPrice(key,priceEntry.getValue());
						}
					}
				}
			}
		}catch (Exception ex){
			ex.printStackTrace();
			updated = false;			
		}
		
		logger.info("Finishing access to provider "+ connector.getIdentifier()+", was updated?  : " + updated );
		result = updated;
		
		return result;
	}
	
	@Override	
	public boolean updateProductTransaction(Product product) throws TooFastConnectionException{
		TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
		AbstractProductInfoConnector connector = this;
		boolean result = true; 		
		result = transactionTemplate.execute(new TransactionCallback<Boolean>(){

			@Override
			public Boolean doInTransaction(TransactionStatus status) {
				logger.info("Starting access to provider : " + connector.getIdentifier());
				boolean updated = false;
				try {
					Collection<Image> imagesAdd = new ArrayList<>();
					Collection<Image> imagesRemove = new ArrayList<>();
					Set<Author> authorsAdd = new HashSet<>();
					
					Product productInDb = productRepository.findOne(product.getId());				
					updated = updateProductDo(productInDb, imagesAdd, imagesRemove,authorsAdd);
					if (updated){	
						logger.info("Saving product");
						productRepository.save(productInDb);
						imageRepository.delete(imagesRemove);
						storeAfterSuccess(productInDb,productRepository);
					}
				}catch (Exception ex){
					ex.printStackTrace();
					updated = false;			
				}
				
				logger.info("Finishing access to provider "+ connector.getIdentifier()+", was updated?  : " + updated );
				return updated;
			}
			
		});
		
		return result;
	}

	public boolean checkIfAlreadyProcessed(Product product) {
		return product!=null && product.getProcessedConnectors()!=null && product.getProcessedConnectors().contains(this.getIdentifier());
	}

			
	protected void storeAfterSuccess(Product product,ProductRepository productRepository) {
		product.addConnector(this.getIdentifier());		
		productRepository.save(product);		
	}
	
	protected boolean updateProductDo(Product product, Collection<Image> imagesAdd, Collection<Image> imagesRemove, Set<Author> authorsAdd) throws TooFastConnectionException, FileNotFoundException{
		boolean processed = false;
		ProductInfoLookupService itemLookup = this.getImageLookupService();		

		if (this.isApplicable(product)){			
			if (!this.checkIfAlreadyProcessed(product)) {
			
				logger.info("Starting process "+this.getIdentifier()+" : " + product.getName());
				
				Object doc = itemLookup.fetchDocFromProduct(product);
				
				if (doc!=null){
					
					String obtainedDescription = null;
					
					obtainedDescription = itemLookup.getDescription(doc);
					logger.info("Obtained Description from Service: " + obtainedDescription);
					if (obtainedDescription!=null){
						boolean overwrite = true;
						String oldDescription = product.getDescription();
						
						product.setDescription(obtainedDescription);
						if (!product.isLengthyDescription()){
							product.setDescription(oldDescription);
							overwrite = false;
						}
						
						if (overwrite 
								&& product.getDescription()!=null 
								&& product.getDescription().length()<obtainedDescription.length()){
							logger.info("Description is lengthier, replacing");
							product.setDescription(obtainedDescription);
						}							
					}				

					String externalLink = null;
					if (product.getExternalLinks()==null || product.getExternalLinks().get(this.getIdentifier())==null){
						
						
						externalLink = itemLookup.getExternalUrlLink(doc);
						logger.info("Obtained Url for "+this.getIdentifier()+": " + externalLink);
							
						if (externalLink!=null){
							Map<String, String> externalLinks = product.getExternalLinks();
							if (externalLinks==null){
								externalLinks = new HashMap<>();
								product.setExternalLinks(externalLinks);								
							}
							externalLinks.put(this.getIdentifier(), externalLink);
						}
					} else {
						logger.info("Already got url? : " + product.getExternalLinks().get(this.getIdentifier()));
					}
					
					String seriesKey = this.getIdentifier() + "Series";
					if (product.getExternalLinks()==null || product.getExternalLinks().get(seriesKey)!=null){
						String seriesUrl = null;
						seriesUrl = itemLookup.getSeriesUrl(doc);
						logger.info("Obtained "+this.getIdentifier()+" series url :" + seriesUrl);
						if (seriesUrl!=null){
							Map<String, String> externalLinks = product.getExternalLinks();
							if (externalLinks==null){
								externalLinks = new HashMap<>();
								product.setExternalLinks(externalLinks);								
							}
							externalLinks.put(seriesKey, seriesUrl);
						}
					}
					
					if (product.getPublisher()==null){
						String publisher = null;
						
						publisher = itemLookup.getPublisher(doc);
						logger.info("Obtained publisher: " + publisher);
							
						if (publisher!=null && !"".equals(publisher.trim())){
							product.setPublisher(publisher);
						}
					}
					
					byte [] imageData = null;
					if (doc!=null){
						imageData = itemLookup.getImageData(doc);
					}
					if (imageData!=null 
							&& (product.getImages()==null 
									|| product.getImages().size()==0 
									|| product.getImages().size()==1)){
						
						Image newImage = new Image();
						newImage.setData(imageData);
						newImage.setMain(true);
						
						boolean executeChange = true;
						if (product.getImages()!=null && product.getImages().size()==1 && product.getImages().get(0).isBigger(newImage)){
							executeChange = false;
						} 
						
						if (executeChange){
							if (product.getImages()!=null && product.getImages().size()>0){
								logger.info("Substituting images: " + product.getImages());
								imagesRemove.addAll(product.getImages());
							}						
														
							logger.info("Obtained image from: " + this.getClass() );
							List<Image> images = new ArrayList<>();
							images.add(newImage);
							imagesAdd.add(newImage);
							product.setImages(images);
						}
					}
					
					if (product.getReleaseDate()==null){
						Integer year = null;
						year = itemLookup.getPublicationYear(doc);
						logger.info("Obtained publication year :" + year);
						if (year!=null){
							Calendar calendar = Calendar.getInstance();
							calendar.set(Calendar.HOUR, 0);
							calendar.set(Calendar.MINUTE, 0);
							calendar.set(Calendar.SECOND, 0);
							calendar.set(Calendar.YEAR,year);
							calendar.set(Calendar.DAY_OF_YEAR,0);
							
							product.setReleaseDate(calendar.getTime());
						}
					}
					
					
					
					
					
					if (product.getAuthors()==null || product.getAuthors().size()==0){
						Set<Author> authors = null;
						authors = itemLookup.getAuthors(doc);
						logger.info("Obtained authors :" + authors);
						if (authors!=null && authors.size()>0){
							logger.info("Obtained " + authors.size() + " authors");
							product.setAuthors(authors);
							authorsAdd.addAll(authors);
						}						
					}	
					
					Map<String,Long> prices = itemLookup.getDollarPrice(doc);					
					if (prices!=null){
						logger.info("Obtained prices :" + prices);
						for (Entry<String,Long> priceEntry: prices.entrySet()){
							String key = this.getIdentifier();
							if (priceEntry.getKey()!=null && !"".equals(priceEntry.getKey().trim())){
								key = key + " - " + priceEntry.getKey(); 
							}
							product.setDollarPrice(key,priceEntry.getValue());
						}
					}
					
					Double rating = itemLookup.getRating(doc);
					if (rating!=null){
						logger.info("Obtained rating :" + rating);
						Map<String, Double> ratings = product.getRatings();
						if (ratings==null){
							ratings = new HashMap<>();
							product.setRatings(ratings);
						}
						ratings.put(this.getIdentifier(), rating);
					}
					
					if (product.getConnectorReferences() == null || product.getConnectorReferences().get(this.getIdentifier())==null){
						String reference = itemLookup.getReference(doc);
						logger.info("Obtained reference :" + reference);
						if (reference!=null){
							Map<String, String> references = product.getConnectorReferences();
							if (references==null){
								references = new HashMap<>();
								product.setConnectorReferences(references);
							}
							references.put(this.getIdentifier(), reference);
						}
					}
					
				} else {
					logger.info(this.getIdentifier() + " o btainednull doc for product: "+ product.getName());
				}
				processed = true;
			} else {
				logger.info("Skipping process, product already processed for product: " + product.getName());
			}
			
						
		} else {
			logger.info("Connector "+this.getIdentifier()+" is not applicable to product: " + product.getName());
		}
		return processed;
	}

	@Override
	public void processInBackground(Collection<Product> products){
		
		Collection<Product> clonedProducts = new ArrayList<Product>();		
		clonedProducts.addAll(products);
		BackgroundProcessor thread = new BackgroundProcessor(clonedProducts, productRepository, imageRepository,authorRepository,  this);
		thread.start();
	}
	
	@Override
	public void processPricesInBackground(Collection<Product> products){
		
		Collection<Product> clonedProducts = new ArrayList<Product>();		
		clonedProducts.addAll(products);
		BackgroundProcessorPrices thread = new BackgroundProcessorPrices(clonedProducts, productRepository, imageRepository,authorRepository,  this);
		thread.start();
	}
}
