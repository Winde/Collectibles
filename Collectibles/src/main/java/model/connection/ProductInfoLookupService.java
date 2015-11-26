package model.connection;

import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import model.dataobjects.Author;
import model.dataobjects.Price;
import model.dataobjects.Product;
import model.dataobjects.Rating;

public interface ProductInfoLookupService<E> {

	public E fetchDocFromProduct(Product product) throws TooFastConnectionException, FileNotFoundException;
	
	public byte [] getMainImageData(E doc) throws TooFastConnectionException;
	
	public List<byte []> getAdditionalImageData(E doc) throws TooFastConnectionException;
	
	public String getDescription(E doc) throws TooFastConnectionException;
	
	public Date getPublicationDate(E doc) throws TooFastConnectionException;
	
	public Set<Author> getAuthors(E doc) throws TooFastConnectionException;
	
	public String getSeriesUrl(E doc) throws TooFastConnectionException;
	
	public String getExternalUrlLink(E doc) throws TooFastConnectionException;

	public String getPublisher(E doc)  throws TooFastConnectionException;
	
	public Collection<Price> getPrices(E doc) throws TooFastConnectionException;
	
	public Rating getRating(E doc) throws TooFastConnectionException;
	
	public String getReference(E doc) throws TooFastConnectionException;
	
	public List<String> getOwnedReferences(String userId) throws TooFastConnectionException;
	
	public String getName(E doc) throws TooFastConnectionException;
	
	public String getIdentifier();
	
	public String getReferenceFromProduct(Product product);
}
