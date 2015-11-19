package model.connection.goodreads;

import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.Set;

import model.connection.AbstractProductInfoConnector;
import model.connection.ProductInfoLookupService;
import model.connection.ProductInfoLookupServiceXML;
import model.connection.TooFastConnectionException;
import model.dataobjects.Author;
import model.dataobjects.Image;
import model.dataobjects.Product;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GoodReadsConnector extends AbstractProductInfoConnector{
	
	private static final Logger logger = LoggerFactory.getLogger(GoodReadsConnector.class);	
	
	
	private static final int SLEEP_BETWEEN_CALLS = 1400;		
	
	@Autowired 
	private GoodReadsItemLookupService itemLookup;
	

	public ProductInfoLookupService getImageLookupService(){
		return itemLookup;
	}

	@Override
	protected boolean updateProductDo(Product product, Collection<Image> imagesAdd, Collection<Image> imagesRemove, Set<Author> authorsAdd) throws TooFastConnectionException, FileNotFoundException{
		boolean result = true;
		try {
			result = super.updateProductDo(product, imagesAdd, imagesRemove,authorsAdd);
		}catch (FileNotFoundException e){	//ForGoodReads FileNotFoundException signifies product not found in database
			logger.error("Detecting file not found, goodreads is rejecting us", e);
			return true;
		}
		return result;
		
	}
	
	@Override
	public String getIdentifier() {
		return itemLookup.getIdentifier();
	}

	public String toString(){
		return "GoodReadsConnector";
	}
	
	@Override
	public boolean isApplicable(Product product) {
		return product!=null && (product.getUniversalReference()!=null 
				|| (product.getConnectorReferences()!=null && product.getConnectorReferences().get(this.getIdentifier())!=null)
			);
	}

	@Override
	public boolean hasOwnReference() {
		return true;
	}

	@Override
	public boolean canCreateLinks() {
		return true;
	}
	
	@Override
	public Integer sleepBetweenCalls() {
		return SLEEP_BETWEEN_CALLS;
	}
}
