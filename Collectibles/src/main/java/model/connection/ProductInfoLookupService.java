package model.connection;

import java.io.FileNotFoundException;
import java.util.List;

import org.w3c.dom.Document;

public interface ProductInfoLookupService<E> {

	public E fetchDocFromId(String id) throws TooFastConnectionException, FileNotFoundException;
	
	public byte [] getImageData(E doc) throws TooFastConnectionException;
	
	public String getDescription(E doc) throws TooFastConnectionException;
	
	public Integer getPublicationYear(E doc) throws TooFastConnectionException;
	
	public List<String> getAuthors(E doc) throws TooFastConnectionException;
	
	public String getSeriesUrl(E doc) throws TooFastConnectionException;

    public String getAmazonUrl(E doc) throws TooFastConnectionException;
    
    public String getDescription(Document doc ) throws TooFastConnectionException;
        
}
