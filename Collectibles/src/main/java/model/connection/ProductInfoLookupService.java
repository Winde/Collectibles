package model.connection;

import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import model.dataobjects.Author;
import model.dataobjects.Product;

import org.jsoup.nodes.Element;
import org.w3c.dom.Document;

public interface ProductInfoLookupService<E> {

	public E fetchDocFromProduct(Product product) throws TooFastConnectionException, FileNotFoundException;
	
	public byte [] getImageData(E doc) throws TooFastConnectionException;
	
	public String getDescription(E doc) throws TooFastConnectionException;
	
	public Integer getPublicationYear(E doc) throws TooFastConnectionException;
	
	public Set<Author> getAuthors(E doc) throws TooFastConnectionException;
	
	public String getSeriesUrl(E doc) throws TooFastConnectionException;
	
	public String getAmazonUrl(E doc) throws TooFastConnectionException;
    
	public String getGoodReadsUrl(E doc) throws TooFastConnectionException;
	
	public String getDrivethrurpgUrl(E doc) throws TooFastConnectionException;
    
	public String getPublisher(E doc)  throws TooFastConnectionException;
	
	public Map<String, Long> getDollarPrice(E doc) throws TooFastConnectionException;
}
