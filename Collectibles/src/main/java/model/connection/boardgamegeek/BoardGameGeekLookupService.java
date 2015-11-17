package model.connection.boardgamegeek;

import java.io.FileNotFoundException;
import java.util.Map;
import java.util.Set;

import model.connection.ProductInfoLookupServiceXML;
import model.connection.TooFastConnectionException;
import model.dataobjects.Author;
import model.dataobjects.Product;

import org.springframework.stereotype.Service;
import org.w3c.dom.Document;

@Service
public class BoardGameGeekLookupService extends ProductInfoLookupServiceXML {

	private static final String IDENTIFIER = "BoardGameGeek";
		
	@Override
	public Document fetchDocFromProduct(Product product)
			throws TooFastConnectionException, FileNotFoundException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte[] getImageData(Document doc) throws TooFastConnectionException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDescription(Document doc)
			throws TooFastConnectionException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer getPublicationYear(Document doc)
			throws TooFastConnectionException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set<Author> getAuthors(Document doc)
			throws TooFastConnectionException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getSeriesUrl(Document doc) throws TooFastConnectionException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getAmazonUrl(Document doc) throws TooFastConnectionException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getGoodReadsUrl(Document doc)
			throws TooFastConnectionException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getDrivethrurpgUrl(Document doc)
			throws TooFastConnectionException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getPublisher(Document doc) throws TooFastConnectionException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Long> getDollarPrice(Document doc)
			throws TooFastConnectionException {
		// TODO Auto-generated method stub
		return null;
	}
	
	public String getIdentifier(){
		return IDENTIFIER;
	}

}
