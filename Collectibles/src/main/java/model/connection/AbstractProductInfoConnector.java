package model.connection;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
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
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

public abstract class AbstractProductInfoConnector implements ProductInfoConnector{

	private static final Logger logger = LoggerFactory.getLogger(AbstractProductInfoConnector.class);
		
	@Autowired
	PlatformTransactionManager transactionManager;
	
	@Autowired
	private ProductRepository productRepository;
	
	@Autowired
	private ImageRepository imageRepository;
	
	@Autowired
	private AuthorRepository authorRepository;
	
	
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
		return product!=null && (product.getProcessedConnectors()==null || !product.getProcessedConnectors().contains(this.getIdentifier()));
	}

			
	protected void storeAfterSuccess(Product product,ProductRepository productRepository) {
		product.addConnector(this.getIdentifier());		
		productRepository.save(product);		
	}
	
	protected boolean updateProductDo(Product product, Collection<Image> imagesAdd, Collection<Image> imagesRemove, Set<Author> authorsAdd) throws TooFastConnectionException, FileNotFoundException{
		boolean processed = false;
		ProductInfoLookupService itemLookup = this.getImageLookupService();		
		
		logger.info("Checking if we have product universal reference");
		
		if (this.isApplicable(product)){
			logger.info("Product universal reference: " +product.getUniversalReference());
			if (this.checkIfAlreadyProcessed(product)) {
			
				logger.info("Starting process");
				
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

					
					if (product.getAmazonUrl()==null){
						String amazonUrl = null;
						
						amazonUrl = itemLookup.getAmazonUrl(doc);
						logger.info("Obtained Url from Amazon: " + amazonUrl);
							
						if (amazonUrl!=null){
							product.setAmazonUrl(amazonUrl);
						}
					}
					
					if (product.getGoodreadsUrl()==null){
						String goodreadsUrl = null;
						
						goodreadsUrl = itemLookup.getGoodReadsUrl(doc);
						logger.info("Obtained Url from Goodreads: " + goodreadsUrl);
							
						if (goodreadsUrl!=null){
							product.setGoodreadsUrl(goodreadsUrl);
						}
					}
					
					if (product.getDrivethrurpgUrl()==null){
						String drivethrurpgUrl = null;
						
						drivethrurpgUrl = itemLookup.getDrivethrurpgUrl(doc);
						logger.info("Obtained Url from Drivethrurpg: " + drivethrurpgUrl);
							
						if (drivethrurpgUrl!=null){
							product.setDrivethrurpgUrl(drivethrurpgUrl);
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
					
					if (product.getGoodreadsRelatedLink()==null){
						String seriesUrl = null;
						seriesUrl = itemLookup.getSeriesUrl(doc);
						logger.info("Obtained series url :" + seriesUrl);
						if (seriesUrl!=null){
							product.setGoodreadsRelatedLink(seriesUrl);
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
				}
				processed = true;
			} else {
				logger.info("Skipping process, product already processed");
			}
			
						
		} else {
			logger.info("NO Product universal reference");
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
}
