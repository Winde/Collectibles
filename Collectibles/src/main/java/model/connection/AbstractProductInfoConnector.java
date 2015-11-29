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
import model.dataobjects.Price;
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
	public boolean updateTransitionalTransaction(Long productId) throws TooFastConnectionException{
		AbstractProductInfoConnector connector = this;		
		Product productInDb = productRepository.findOne(productId);
		boolean updated = false;
		if (productInDb!=null){			
			try {
				updated = connector.updateTransitional(productInDb);
			} catch (TooFastConnectionException e) {
				logger.error("Too fast connection to: "+connector.getIdentifier() , e);
			}
			if (updated){
				productRepository.save(productInDb);
			}
		}
		return updated;
	}
		
	public boolean updateTransitional(Product product) throws TooFastConnectionException{
		AbstractProductInfoConnector connector = this;
		boolean result = true; 		

		logger.debug("Starting access to provider : " + connector.getIdentifier());
		boolean updated = false;
		try {
								
			ProductInfoLookupService itemLookup = connector.getProductInfoLookupService();										
			if (itemLookup!=null){
				Object doc = itemLookup.fetchDocFromProductForTransient(product);
				if (doc!=null){
					
				
					updated = updated || updatePrice(product,doc);
					
					updated = updated || updateRating(product,doc);
					
					logger.info("Saving product with ratings: " + product.getRatings());
					logger.info("Saving product with prices: " + product.getPrices());
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
	public boolean updateProductWithoutSave(Long productId) throws TooFastConnectionException{
		return updateProductTransaction(productId,false);
	}
	
	@Override
	public boolean updateProductTransaction(Long productId) throws TooFastConnectionException{
		return updateProductTransaction(productId,true);
	}
	
		
	private boolean updateProductTransaction(Long productId, boolean save) throws TooFastConnectionException{		
		AbstractProductInfoConnector connector = this;
		boolean result = true; 		
		boolean updated = false;
		
		Product product = productRepository.findOne(productId);
		if (product!=null){
			logger.info("Starting access to provider : " + connector.getIdentifier());
			
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
		} else{
			logger.error("Trying to update non existing product? " + productId);
		}
		
		return updated;
	}

	public boolean checkIfAlreadyProcessed(Product product) {
		return product!=null && product.getProcessedConnectors()!=null && product.getProcessedConnectors().contains(this.getIdentifier());
	}
			
	protected void storeAfterSuccess(Product product,ProductRepository productRepository) {
		product.addConnector(this.getIdentifier());		
		productRepository.save(product);		
	}
	
	
	private void updateName(Product product, Object doc) throws TooFastConnectionException{
		if (product.getName()==null || "".equals(product.getName().trim())){
			String name = this.getProductInfoLookupService().getName(doc);
			logger.info(this.getIdentifier() + " Obtained name for" + product);
			product.setName(name);
		}					
	}
	
	private void updateDescription(Product product, Object doc) throws TooFastConnectionException{
		String obtainedDescription = null;					
		obtainedDescription = this.getProductInfoLookupService().getDescription(doc);					
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
	}
	
	private void updateExternalLinks(Product product, Object doc) throws TooFastConnectionException{
		String externalLink = null;
		if (product.getExternalLinks()!=null){
			externalLink = product.getExternalLinks().get(this.getIdentifier());
		}
		if (externalLink==null || "".equals(externalLink.trim()) ){
			
			
			externalLink = this.getProductInfoLookupService().getExternalUrlLink(doc);
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
	}
	
	private void updateRelatedLinks(Product product, Object doc) throws TooFastConnectionException{
		logger.debug(this.getIdentifier() + " Checking related links");
		String seriesKey = this.getIdentifier() + "Series";
		String externalRelatedLink = null;
		if (product.getExternalLinks()!=null){
			externalRelatedLink = product.getExternalLinks().get(seriesKey);
		}
		if (externalRelatedLink == null || "".equals(externalRelatedLink.trim())){
			String seriesUrl = null;
			seriesUrl = this.getProductInfoLookupService().getSeriesUrl(doc);
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
	}
	
	private void updatePublisher(Product product, Object doc) throws TooFastConnectionException{

		logger.debug(this.getIdentifier() + " Checking Publisher");
		if (product.getPublisher()==null || "".equals(product.getPublisher().trim())){
			String publisher = null;
			
			publisher = this.getProductInfoLookupService().getPublisher(doc);
			logger.info(this.getIdentifier()+" obtained publisher: " + publisher);
				
			if (publisher!=null && !"".equals(publisher.trim())){
				product.setPublisher(publisher);
			}
		}
	}
	
	private void updateMainImage(Product product, Object doc, Collection<Image> imagesAdd, Collection<Image> imagesRemove ) throws TooFastConnectionException{
		logger.debug(this.getIdentifier() + " Checking Main image");
		byte [] imageData = null;
		if (doc!=null){
			imageData = this.getProductInfoLookupService().getMainImageData(doc);
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
	}

	private void updateAdditionalImages(Product product, Object doc, Collection<Image> imagesAdd ) throws TooFastConnectionException{
		logger.debug(this.getIdentifier() + " Checking Additional images");
		if (product.getImages()!=null && product.getImages().size()==1){
			List<byte []> imagesData = this.getProductInfoLookupService().getAdditionalImageData(doc);
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
	}
	
	private void updateReleaseDate(Product product, Object doc) throws TooFastConnectionException{
		logger.debug(this.getIdentifier() + " Checking Release date");
		if (product.getReleaseDate()==null){
			Date date = null;
			date = this.getProductInfoLookupService().getPublicationDate(doc);
			logger.info(this.getIdentifier() + "Obtained publication date :" + date);
			if (date!=null){							
				product.setReleaseDate(date);
			}
		}
		
	}
	
	private void updateAuthors(Product product, Object doc, Collection<Author> authorsAdd) throws TooFastConnectionException{
		logger.debug(this.getIdentifier() + " Checking Authors");					
		if (product.getAuthors()==null || product.getAuthors().size()==0){
			Set<Author> authors = null;
			authors = this.getProductInfoLookupService().getAuthors(doc);						
			if (authors!=null && authors.size()>0){
				logger.info(this.getIdentifier() + "obtained authors for product "+product+ ":" + authors);
				product.setAuthors(authors);
				authorsAdd.addAll(authors);
			}						
		}
	}
	
	private boolean  updatePrice(Product product, Object doc) throws TooFastConnectionException{
		boolean updated = false;
		logger.debug(this.getIdentifier() + " Checking Dollar Price");					
		Collection<Price> prices = this.getProductInfoLookupService().getPrices(doc);
		product.removePrice(this.getIdentifier());
		if (prices!=null){
			logger.info(this.getIdentifier() + " obtained prices for product "+product+":" + prices);
			Iterator<Price> iterator = prices.iterator();			
			Price price = null;			
			while (iterator.hasNext()){
				price  = iterator.next();
				price.setProduct(product);
				product.addPrice(price);
				updated = true;
			}						
		}		
		return updated;
	}
	
	private boolean updateRating(Product product, Object doc) throws TooFastConnectionException{
		logger.debug(this.getIdentifier() + " Checking Rating");
		boolean updated = false;
		Rating rating = null;
		if (this.guaranteeUnivocalResponse(product)){
			rating = this.getProductInfoLookupService().getRating(doc);						
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
		return updated;
	}
	
	private void updateReference(Product product, Object doc) throws TooFastConnectionException{
		logger.debug(this.getIdentifier() + " Checking Reference");					
		String connectorReference = null;
		if (product.getConnectorReferences()!=null){
			connectorReference = product.getConnectorReferences().get(this.getIdentifier());
		}
		if (connectorReference== null || "".equals(connectorReference.trim())){
			String reference = this.getProductInfoLookupService().getReference(doc);
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
		
	}
	
	protected boolean updateProductDo(Product product, Collection<Image> imagesAdd, Collection<Image> imagesRemove, Set<Author> authorsAdd) throws TooFastConnectionException, FileNotFoundException{
		boolean processed = false;
		ProductInfoLookupService itemLookup = this.getProductInfoLookupService();		

		if (this.isApplicable(product)){						
			
				logger.info("Starting process "+this.getIdentifier()+" : " + product.getName());
				
				Object doc = itemLookup.fetchDocFromProduct(product);
				
				logger.debug("Obtained doc: " + doc);
				
				if (doc!=null){
					
					
					updateName(product,doc);
					
					updateDescription(product,doc);				
					
					updateExternalLinks(product,doc);
										
					updateRelatedLinks(product,doc);
					
					updatePublisher(product,doc);
					
					updateMainImage(product,doc,imagesAdd,imagesRemove);
					
					updateAdditionalImages(product,doc,imagesAdd);
					
					updateReleaseDate(product,doc);
										
					updateAuthors(product,doc,authorsAdd);	
					
					updatePrice(product,doc);
					
					updateRating(product,doc);
					
					updateReference(product,doc);
															
					
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
	
	@Override
	public int compareTo(ProductInfoConnector o) {
		if (this.getIdentifier()==null || o.getIdentifier()==null){
			return -1;
		} else{
			return this.getIdentifier().compareTo(o.getIdentifier());
		}
	}
}
