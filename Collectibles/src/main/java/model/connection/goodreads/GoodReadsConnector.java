package model.connection.goodreads;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;

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
	private GoodReadsItemLookupService lookUpService;
	

	public ProductInfoLookupService getImageLookupService(){
		return lookUpService;
	}



	@Override
	protected boolean updateProductDo(Product product, Collection<Image> imagesAdd, Collection<Image> imagesRemove, Collection<Author> authorsAdd) throws TooFastConnectionException, FileNotFoundException{
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
	protected boolean checkIfWeProcess(Product product) {
		return product!=null && (product.getIsGoodreadsProcessed()==null || !product.getIsGoodreadsProcessed());
	}

	@Override
	protected void storeAfterSuccess(Product product,
			ProductRepository productRepository) {
		product.setIsGoodreadsProcessed(Boolean.TRUE);
		productRepository.save(product);	
	}


	
	
}
