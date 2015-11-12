package model.connection;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;

import javax.transaction.Transactional;

import model.dataobjects.Author;
import model.dataobjects.Image;
import model.dataobjects.Product;
import model.persistence.AuthorRepository;
import model.persistence.ImageRepository;
import model.persistence.ProductRepository;

import org.w3c.dom.Document;

public abstract class AbstractProductInfoConnector implements ProductInfoConnector{

	@Override
	public boolean updateProductTransaction(Product product,ProductRepository productRepository, ImageRepository imageRepository, AuthorRepository authorRepository) throws TooFastConnectionException{		
		System.out.println("Starting access to provider : " + this.getClass());
		boolean updated = false;
		try {
			Collection<Image> imagesAdd = new ArrayList<>();
			Collection<Image> imagesRemove = new ArrayList<>();
			Collection<Author> authorsAdd = new ArrayList<>();
			
			productRepository.findOne(product.getId());				
			updated = this.updateProductDo(product, imagesAdd, imagesRemove,authorsAdd);
			if (updated){	
				//System.out.println("Removing images");
				//System.out.println(imagesRemove);
				//imageRepository.delete(imagesRemove);
				System.out.println("Saving product");
				productRepository.save(product);
				this.storeAfterSuccess(product,productRepository);
			}
		}catch (Exception ex){
			ex.printStackTrace();
			updated = false;			
		}
		
		System.out.println("Finishing access to provider "+this.getClass()+", was updated?  : " + updated );
		return updated;
	}

	
	
	
	protected abstract boolean checkIfWeProcess(Product product);
	
	protected abstract void storeAfterSuccess(Product product, ProductRepository productRepository); 
	
	
	protected boolean updateProductDo(Product product, Collection<Image> imagesAdd, Collection<Image> imagesRemove, Collection<Author> authorsAdd) throws TooFastConnectionException, FileNotFoundException{
		boolean processed = false;
		ProductInfoLookupService itemLookup = this.getImageLookupService();
		List<Image> removeImages = new ArrayList<>();
		
		System.out.println("Checking if we have product universal reference");
		
		if (product.getUniversalReference()!=null){
			System.out.println("Product universal reference: " +product.getUniversalReference());
			if (checkIfWeProcess(product)) {
			
				System.out.println("Starting process");
				
				Object doc = itemLookup.fetchDocFromId(product.getUniversalReference());
				
				if (doc!=null){
					
					String obtainedDescription = null;
					
					obtainedDescription = itemLookup.getDescription(doc);
					System.out.println("Obtained Description from Service: " + obtainedDescription);
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
							System.out.println("Description is lengthier, replacing");
							product.setDescription(obtainedDescription);
						}							
					}				

					
					if (product.getAmazonUrl()==null){
						String amazonUrl = null;
						
						amazonUrl = itemLookup.getAmazonUrl(doc);
						System.out.println("Obtained Url from Amazon: " + amazonUrl);
							
						if (amazonUrl!=null){
							product.setAmazonUrl(amazonUrl);
						}
					}
					
					if (product.getGoodreadsUrl()==null){
						String goodreadsUrl = null;
						
						goodreadsUrl = itemLookup.getGoodReadsUrl(doc);
						System.out.println("Obtained Url from Goodreads: " + goodreadsUrl);
							
						if (goodreadsUrl!=null){
							product.setGoodreadsUrl(goodreadsUrl);
						}
					}
					
					if (product.getPublisher()==null){
						String publisher = null;
						
						publisher = itemLookup.getPublisher(doc);
						System.out.println("Obtained publisher: " + publisher);
							
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
								removeImages.addAll(product.getImages());
							}						
							
							imagesAdd.add(newImage);
							
							System.out.println("Obtained image from: " + this.getClass() );
							List<Image> images = new ArrayList<>();
							images.add(newImage);							
							product.setImages(images);
						}
					}
					
					if (product.getReleaseDate()==null){
						Integer year = null;
						year = itemLookup.getPublicationYear(doc);
						System.out.println("Obtained publication year :" + year);
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
						System.out.println("Obtained series url :" + seriesUrl);
						if (seriesUrl!=null){
							product.setGoodreadsRelatedLink(seriesUrl);
						}
					}
					
					if (product.getAuthors()==null || product.getAuthors().size()==0){
						Collection<Author> authors = null;
						authors = itemLookup.getAuthors(doc);
						System.out.println("Obtained authors :" + authors);
						if (authors!=null && authors.size()>0){
							System.out.println("Obtained " + authors.size() + " authors");
							product.setAuthors(authors);
							authorsAdd.addAll(authors);
						}
						
					}					
				}
				processed = true;
			} else {
				System.out.println("Skipping process, product already processed");
			}
			
						
		} else {
			System.out.println("NO Product universal reference");
		}
		return processed;
	}
	
}
