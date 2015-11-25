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
import java.util.SortedMap;
import java.util.TreeMap;

import model.dataobjects.Author;
import model.dataobjects.Image;
import model.dataobjects.Product;
import model.dataobjects.Rating;
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
	public boolean updateTransitionalTransaction(Product product) throws TooFastConnectionException{
		AbstractProductInfoConnector connector = this;		
		Product productInDb = productRepository.findOne(product.getId());
		boolean updated = false;
		try {
			updated = connector.updateTransitional(productInDb);
		} catch (TooFastConnectionException e) {
			logger.error("Too fast connection to: "+connector.getIdentifier() , e);
		}
		if (updated){
			productRepository.save(productInDb);
		}
		return updated;
	}
	
	@Override	
	public boolean updateTransitional(Product product) throws TooFastConnectionException{
		AbstractProductInfoConnector connector = this;
		boolean result = true; 		

		logger.debug("Starting access to provider : " + connector.getIdentifier());
		boolean updated = false;
		try {
								
			ProductInfoLookupService itemLookup = connector.getProductInfoLookupService();										
			if (itemLookup!=null){
				Object doc = itemLookup.fetchDocFromProduct(product);
				if (doc!=null){
					
					Map<String,Long> prices = null;
					if (connector.guaranteeUnivocalResponse(product)){
						prices = itemLookup.getDollarPrice(doc);
						logger.info(this.getIdentifier()+" obtained prices: " + prices +" for " + product);
					} else {
						logger.info(this.getIdentifier() + " Skip prices for" + product);
					}
					if (product.getDollarPrice()!=null){
						boolean modifiedMinPrice = false;
						Set<Entry<String, Long>> entries = product.getDollarPrice().entrySet();
						if (entries!=null){
							Iterator<Entry<String, Long>> iterator = entries.iterator();
							while (iterator.hasNext()){
								String key = iterator.next().getKey();
								if (key!=null && key.startsWith(this.getIdentifier())){
									iterator.remove();
									modifiedMinPrice = true;
								}
							}
						}
						if (modifiedMinPrice){
							product.setMinPrice(product.calculateMinDollarPrice());
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
						updated = true;
					} 
					
					logger.debug(this.getIdentifier() + " Checking Rating");
					Rating rating = null;
					if (connector.guaranteeUnivocalResponse(product)){
						rating = itemLookup.getRating(doc);						
						logger.info(this.getIdentifier() + " Obtained rating"+ rating +"for" + product);
					} else {
						logger.info(this.getIdentifier() + " Skip rating for" + product);
					}
					if (rating!=null){
						rating.setProduct(product);
						if (product.getRatings()==null){
							product.setMainRating(rating.getRating());
						}
						product.addRating(rating);
						updated = true;
					} else {
						product.removeRating(this.getIdentifier());
						updated = true;
					}
					
					logger.info("Saving product with ratings: " + product.getRatings());
					logger.info("Saving product with prices: " + product.getDollarPrice());
				}
			}
		}catch (Exception e){
			logger.error("Exception when updating price", e);
			updated = false;			
		}
		
		logger.info("Finishing access to Transitional provider "+ connector.getIdentifier()+", was updated?  : " + updated );
		result = updated;
		
		return result;
	}
	
	@Override
	public boolean updateProductWithoutSave(Product product) throws TooFastConnectionException{
		return updateProductTransaction(product,false);
	}
	
	@Override
	public boolean updateProductTransaction(Product product) throws TooFastConnectionException{
		return updateProductTransaction(product,true);
	}
	
		
	private boolean updateProductTransaction(Product product, boolean save) throws TooFastConnectionException{		
		AbstractProductInfoConnector connector = this;
		boolean result = true; 		
		
		logger.info("Starting access to provider : " + connector.getIdentifier());
		boolean updated = false;
		try {
			Collection<Image> imagesAdd = new ArrayList<>();
			Collection<Image> imagesRemove = new ArrayList<>();
			Set<Author> authorsAdd = new HashSet<>();				
			logger.debug("Looking for product in database:" + product);
			if (product.getId()!=null){
				product = productRepository.findOne(product.getId());					
				logger.debug("Product found in database:" + product);				
			}
			updated = updateProductDo(product, imagesAdd, imagesRemove,authorsAdd);
			if (updated && save){	
				logger.info("Saving product " + product);
				productRepository.save(product);
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
		ProductInfoLookupService itemLookup = this.getProductInfoLookupService();		

		if (this.isApplicable(product)){						
			
				logger.info("Starting process "+this.getIdentifier()+" : " + product.getName());
				
				Object doc = itemLookup.fetchDocFromProduct(product);
				
				logger.debug("Obtained doc: " + doc);
				
				if (doc!=null){
					
					
					if (product.getName()==null || "".equals(product.getName().trim())){
						String name = itemLookup.getName(doc);
						logger.info(this.getIdentifier() + " Obtained name for" + product);
						product.setName(name);
					}					
					
					String obtainedDescription = null;					
					obtainedDescription = itemLookup.getDescription(doc);					
					if (obtainedDescription!=null){
						logger.info(this.getIdentifier() + " Obtained Description from Service for " + product);
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
					
					String externalLink = null;
					if (product.getExternalLinks()!=null){
						externalLink = product.getExternalLinks().get(this.getIdentifier());
					}
					if (externalLink==null || "".equals(externalLink.trim()) ){
						
						
						externalLink = itemLookup.getExternalUrlLink(doc);
						logger.info(this.getIdentifier()+ " Obtained Url for "+ product +": " + externalLink);
							
						if (externalLink!=null){
							SortedMap<String, String> externalLinks = product.getExternalLinks();
							if (externalLinks==null){
								externalLinks = new TreeMap<>();
								product.setExternalLinks(externalLinks);								
							}
							externalLinks.put(this.getIdentifier(), externalLink);
						}
					} else {
						logger.debug("Already got url? : " + product.getExternalLinks().get(this.getIdentifier()));
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
						logger.info(this.getIdentifier() + "Obtained for product" +product +" series url :" + seriesUrl);
						if (seriesUrl!=null){
							SortedMap<String, String> externalLinks = product.getExternalLinks();
							if (externalLinks==null){
								externalLinks = new TreeMap<>();
								product.setExternalLinks(externalLinks);								
							}
							externalLinks.put(seriesKey, seriesUrl);
						}
					}
					
					logger.debug(this.getIdentifier() + " Checking Publisher");
					if (product.getPublisher()==null || "".equals(product.getPublisher().trim())){
						String publisher = null;
						
						publisher = itemLookup.getPublisher(doc);
						logger.info(this.getIdentifier()+" obtained publisher: " + publisher);
							
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
						newImage.setProduct(product);
						
						boolean executeChange = true;
						if (product.getImages()!=null && product.getImages().size()==1 && product.getImages().get(0).isBigger(newImage)){
							executeChange = false;
						}
						
						if (executeChange){
							if (product.getImages()!=null && product.getImages().size()>0){
								logger.info(this.getIdentifier() + " Substituting images for product " +product+": " + product.getImages());
								imagesRemove.addAll(product.getImages());
							}						
																					
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
							logger.info(this.getIdentifier() + " Obtained additinal images for " +product+" (" + imagesData.size()+")");
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
						logger.info(this.getIdentifier() + "Obtained publication date :" + date);
						if (date!=null){							
							product.setReleaseDate(date);
						}
					}
					
					
					
					logger.debug(this.getIdentifier() + " Checking Authors");
					
					if (product.getAuthors()==null || product.getAuthors().size()==0){
						Set<Author> authors = null;
						authors = itemLookup.getAuthors(doc);						
						if (authors!=null && authors.size()>0){
							logger.info(this.getIdentifier() + "obtained authors for product "+product+ ":" + authors);
							product.setAuthors(authors);
							authorsAdd.addAll(authors);
						}						
					}	
					
					logger.debug(this.getIdentifier() + " Checking Dollar Price");
					
					Map<String,Long> prices = itemLookup.getDollarPrice(doc);					
					if (prices!=null){
						logger.info(this.getIdentifier() + "obtained prices for product "+product+":" + prices);
						for (Entry<String,Long> priceEntry: prices.entrySet()){
							String key = this.getIdentifier();
							if (priceEntry.getKey()!=null && !"".equals(priceEntry.getKey().trim())){
								key = key + " - " + priceEntry.getKey(); 
							}
							product.setDollarPrice(key,priceEntry.getValue());
						}						
					}
					product.setMinPrice(product.calculateMinDollarPrice());
					
					logger.debug(this.getIdentifier() + " Checking Rating");
					Rating rating = null;					
					rating = itemLookup.getRating(doc);
					logger.info(this.getIdentifier() + " Obtained rating"+ rating +"for" + product);				
					if (rating!=null){		
						rating.setProduct(product);
						if (product.getRatings()==null){
							product.setMainRating(rating.getRating());
						}
						product.addRating(rating);
					} else {
						product.removeRating(this.getIdentifier());
					}
					
					logger.debug(this.getIdentifier() + " Checking Reference");					
					String connectorReference = null;
					if (product.getConnectorReferences()!=null){
						connectorReference = product.getConnectorReferences().get(this.getIdentifier());
					}
					if (connectorReference== null || "".equals(connectorReference.trim())){
						String reference = itemLookup.getReference(doc);
						logger.info(this.getIdentifier() + " obtained reference for product "+product +":" + reference);
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
	
	public String getIdentifier(){
		return this.getProductInfoLookupService().getIdentifier();
	}
	
	public List<String> getMultipleReferences(String criteria) throws TooFastConnectionException{
		//Unsupported by default
		return null;
	}

	public boolean supportsImportingProducts(){
		//Unsupported by default
		return false;
	}
	
	public boolean supportsTransientData(){
		return this.supportsPrices() || this.supportsRating();
	}
	
	public boolean guaranteeUnivocalResponse(Product product){
		String reference = this.getProductInfoLookupService().getReferenceFromProduct(product);
		return reference!=null;
	}
}
