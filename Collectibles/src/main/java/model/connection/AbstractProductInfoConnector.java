package model.connection;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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
import org.springframework.transaction.annotation.Transactional;

@Transactional
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
		AbstractProductInfoConnector connector = this;		
		Product productInDb = productRepository.findOne(product.getId());
		boolean updated = false;
		try {
			updated = connector.updatePrice(productInDb);
		} catch (TooFastConnectionException e) {
			logger.error("Too fast connection to: "+connector.getIdentifier() , e);
		}
		if (updated){
			productRepository.save(productInDb);
		}
		return updated;
	}
	
	@Override	
	public boolean updatePrice(Product product) throws TooFastConnectionException{
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
					logger.info(this.getIdentifier()+" obtained prices: " + prices);
					
					if (product.getDollarPrice()!=null){
						Set<Entry<String, Long>> entries = product.getDollarPrice().entrySet();
						if (entries!=null){
							Iterator<Entry<String, Long>> iterator = entries.iterator();
							while (iterator.hasNext()){
								String key = iterator.next().getKey();
								if (key!=null && key.startsWith(this.getIdentifier())){
									iterator.remove();
								}
							}
						}
					}
					
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
		}catch (Exception e){
			logger.error("Exception when updating price", e);
			updated = false;			
		}
		
		logger.info("Finishing access to price provider "+ connector.getIdentifier()+", was updated?  : " + updated );
		result = updated;
		
		return result;
	}
	
	public boolean updateProductTransaction(Product product) throws TooFastConnectionException{
		return updateProductTransaction(product,true);
	}
	
	@Override	
	public boolean updateProductTransaction(Product product, boolean save) throws TooFastConnectionException{		
		AbstractProductInfoConnector connector = this;
		boolean result = true; 		
		
		logger.info("Starting access to provider : " + connector.getIdentifier());
		boolean updated = false;
		try {
			Collection<Image> imagesAdd = new ArrayList<>();
			Collection<Image> imagesRemove = new ArrayList<>();
			Set<Author> authorsAdd = new HashSet<>();				
			logger.info("Looking for product in database:" + product);
			Product productInDb = productRepository.findOne(product.getId());					
			logger.info("Product found in database:" + productInDb);
			updated = updateProductDo(productInDb, imagesAdd, imagesRemove,authorsAdd);
			if (updated && save){	
				logger.info("Saving product");
				productRepository.save(productInDb);
				imageRepository.delete(imagesRemove);
				//storeAfterSuccess(productInDb,productRepository);
			}
		}catch (Exception e){
			logger.error("Exception when scraping", e);
			updated = false;			
		}
		
		logger.info("Finishing access to provider "+ connector.getIdentifier()+", was updated?  : " + updated );
		return updated;
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
			
				logger.info("Starting process "+this.getIdentifier()+" : " + product.getName());
				
				Object doc = itemLookup.fetchDocFromProduct(product);
				
				logger.info("Obtained doc: " + doc);
				
				if (doc!=null){
					
					logger.debug(this.getIdentifier() + " Checking product name");
					if (product.getName()==null || "".equals(product.getName().trim())){
						String name = itemLookup.getName(doc);
						product.setName(name);
					}					
					
					String obtainedDescription = null;					
					obtainedDescription = itemLookup.getDescription(doc);
					logger.debug(this.getIdentifier() + " Obtained Description from Service: " + obtainedDescription);
					if (obtainedDescription!=null){
						if (product.getDescription()==null || "".equals(product.getDescription().trim()) || !product.isLengthyDescription()){
							if (product.getDescription()!=null){
								if (obtainedDescription.length()>product.getDescription().length()){
									product.setDescription(obtainedDescription);
								}
							} else {
								product.setDescription(obtainedDescription);
							}
						}
					}				

					logger.debug(this.getIdentifier() + " Checking external links");
					String externalLink = null;
					if (product.getExternalLinks()!=null){
						externalLink = product.getExternalLinks().get(this.getIdentifier());
					}
					if (externalLink==null || "".equals(externalLink.trim()) ){
						
						
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
					
					
					logger.debug(this.getIdentifier() + " Checking related links");
					String seriesKey = this.getIdentifier() + "Series";
					String externalRelatedLink = null;
					if (product.getExternalLinks()!=null){
						externalRelatedLink = product.getExternalLinks().get(seriesKey);
					}
					if (externalRelatedLink == null || "".equals(externalRelatedLink.trim())){
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
					
					logger.debug(this.getIdentifier() + " Checking Publisher");
					if (product.getPublisher()==null || "".equals(product.getPublisher().trim())){
						String publisher = null;
						
						publisher = itemLookup.getPublisher(doc);
						logger.info("Obtained publisher: " + publisher);
							
						if (publisher!=null && !"".equals(publisher.trim())){
							product.setPublisher(publisher);
						}
					}
					
					logger.debug(this.getIdentifier() + " Checking Main image");
					byte [] imageData = null;
					if (doc!=null){
						imageData = itemLookup.getMainImageData(doc);
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
					
					logger.debug(this.getIdentifier() + " Checking Additional images");
					if (product.getImages()!=null && product.getImages().size()==1){
						List<byte []> imagesData = itemLookup.getAdditionalImageData(doc);
						if (imagesData!=null){
							for (byte[] imageBytes : imagesData) {
								Image image = new Image();							
								image.setData(imageBytes);
								imagesAdd.add(image);
								product.addImage(image);							
							}
						}
					}
					
					logger.debug(this.getIdentifier() + " Checking Release date");
					if (product.getReleaseDate()==null){
						Date date = null;
						date = itemLookup.getPublicationDate(doc);
						logger.info("Obtained publication date :" + date);
						if (date!=null){							
							product.setReleaseDate(date);
						}
					}
					
					
					
					logger.debug(this.getIdentifier() + " Checking Authors");
					
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
					
					logger.debug(this.getIdentifier() + " Checking Dollar Price");
					
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
					
					logger.debug(this.getIdentifier() + " Checking Rating");
					
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
					
					logger.debug(this.getIdentifier() + " Checking Reference");					
					String connectorReference = null;
					if (product.getConnectorReferences()!=null){
						connectorReference = product.getConnectorReferences().get(this.getIdentifier());
					}
					if (connectorReference== null || "".equals(connectorReference.trim())){
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
					logger.info(this.getIdentifier() + " obtained null doc for product: "+ product.getName());
				}
				processed = true;
						
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
	
	public String getIdentifier(){
		return this.getImageLookupService().getIdentifier();
	}
}
