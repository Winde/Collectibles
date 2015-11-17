package model.connection.goodreads;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import model.connection.AbstractProductInfoConnector;
import model.connection.BackgroundProcessor;
import model.connection.ProductInfoLookupService;
import model.connection.TooFastConnectionException;
import model.dataobjects.Author;
import model.dataobjects.Image;
import model.dataobjects.Product;
import model.persistence.AuthorRepository;
import model.persistence.ImageRepository;
import model.persistence.ProductRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;

@Component
public class GoodReadsConnector extends AbstractProductInfoConnector{
	
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
		}catch (FileNotFoundException ex){	//ForGoodReads FileNotFoundException signifies product not found in database
			ex.printStackTrace();
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
	
	
}
