package model.connection.boardgamegeek;

import java.io.FileNotFoundException;
import java.util.Map;
import java.util.Set;

import model.connection.ProductInfoLookupServiceXML;
import model.connection.TooFastConnectionException;
import model.connection.amazon.AmazonItemLookupService;
import model.dataobjects.Author;
import model.dataobjects.Product;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;

@Service
public class BoardGameGeekLookupService extends ProductInfoLookupServiceXML {

	private static final Logger logger = LoggerFactory.getLogger(BoardGameGeekLookupService.class);
	
	private static final String IDENTIFIER = "BoardGameGeek";
		
	@Override
	public Document fetchDocFromProduct(Product product)
			throws TooFastConnectionException, FileNotFoundException {
		String reference = null;
		if (product.getConnectorReferences()!=null && product.getConnectorReferences().get(this.getIdentifier())!=null){
			reference = product.getConnectorReferences().get(this.getIdentifier());		
		}
		if (reference==null || "".equals(reference.trim())){
			reference = product.getUniversalReference();
		}
		if (reference == null){
			return null;
		}
		String url = null;
		if (product.getConnectorReferences()!=null && product.getConnectorReferences().get(this.getIdentifier())!=null){
			url = "https://www.boardgamegeek.com/xmlapi2/thing?stats=1";		
			url = url + "&id=" + reference;
		}
		logger.info("BoardgameGeek url for fetch data: " + url);
		if (url==null){
			return null;
		} else {
			return super.fetchDocFromUrl(url);
		}
	}

	@Override
	public byte[] getImageData(Document doc) throws TooFastConnectionException {
		byte[] image = null;
		String imageUrl = super.getField(doc, "/items/item/image");
		if (imageUrl!=null) {
			if (imageUrl.startsWith("//")){
				imageUrl = "http:"+imageUrl;
			}
			logger.info("Image url: " + imageUrl);
			image = super.fetchImage(imageUrl);
		}
		return image;
	}

	@Override
	public String getDescription(Document doc) throws TooFastConnectionException {
		return super.getField(doc, "/items/item/description");
	}

	@Override
	public Integer getPublicationYear(Document doc) throws TooFastConnectionException {
		Integer year = null;
		String yearString = super.getAttribute(doc, "/items/item/yearpublished", "value");
		if (yearString!=null){
			try {
				year = Integer.parseInt(yearString);
			}catch (Exception ex){
				ex.printStackTrace();
			}
		}
		return year;
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
    public String getExternalUrlLink(Document doc){
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getPublisher(Document doc) throws TooFastConnectionException {
		//String publisher = super.getAttribute(doc, "/items/item/link[@type='boardgamepublisher']","value");		
		return null;
	}

	@Override
	public Map<String, Long> getDollarPrice(Document doc)
			throws TooFastConnectionException {
		// TODO Auto-generated method stub
		return null;
	}
	

	@Override
	public Double getRating(Document doc) throws TooFastConnectionException {
		Double rating = null;
		String ratingString = super.getAttribute(doc, "/items/item/statistics/ratings/bayesaverage","value");		
		if (ratingString!=null){
			try{
				rating = Double.parseDouble(ratingString);
			}catch (Exception ex){
				ex.printStackTrace();
			}
		}
		
		return rating;
	}
	
	public String getIdentifier(){
		return IDENTIFIER;
	}


}
