package model.connection.boardgamegeek;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import model.connection.ProductInfoLookupServiceXML;
import model.connection.TooFastConnectionException;
import model.connection.amazon.AmazonItemLookupService;
import model.dataobjects.Author;
import model.dataobjects.Product;

import org.apache.commons.lang.StringUtils;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@Service
public class BoardGameGeekLookupService extends ProductInfoLookupServiceXML {

	private static final Logger logger = LoggerFactory.getLogger(BoardGameGeekLookupService.class);
	

	private static final String IDENTIFIER = "BoardGameGeek";
	
	private String PRODUCT_URL = null;
	private String ENDPOINTv1 = null;
	private String ENDPOINTv2 = null;
	
	private final String gameNameDocPath = "/boardgames/boardgame/name";
	private final String imageUrlDocPath = "/items/item/image";
	private final String itemDescriptionDocPath = "/items/item/description";
	
	private final String yearPublishedDocPath = "/items/item/yearpublished";
	private final String yearPublishedAttribute = "value";
	
	private final String ratingDocPath = "/items/item/statistics/ratings/bayesaverage";
	private final String ratingAttribute = "value";	
	
	private final String referenceDocPath = "/items/item";
	private final String referenceAttribute = "id";
			
	@Autowired
	public BoardGameGeekLookupService(
			@Value("${ENDPOINTv1}")String ENDPOINTv1,
			@Value("${ENDPOINTv2}")String ENDPOINTv2,
			@Value("${PRODUCT_URL}")String PRODUCT_URL){
		this.PRODUCT_URL = PRODUCT_URL;
		this.ENDPOINTv1 = ENDPOINTv1;
		this.ENDPOINTv2 = ENDPOINTv2;
	}
	
	private String getIdFromName(String name){
		String reference = null;
		String url = null;
		try {
			url = this.ENDPOINTv1 +"?search="+URLEncoder.encode(name, "UTF-8");			
		} catch (UnsupportedEncodingException e) {
			logger.error("Exception when encoding URL", e);
		}
		
		logger.info("Fetching boardgamesgeek url to search by name: " +url);
		
		if (url!=null){
			Document doc = null;
			 try {
				 doc = super.fetchDocFromUrl(url);
			} catch (FileNotFoundException | TooFastConnectionException e) {
				logger.error("Exception when accesing Doc", e);
			}
			 
			if (doc!=null){
				NodeList nodes = super.getNodes(doc, gameNameDocPath);
				if (nodes!=null){
				
					List<String> productNames = new ArrayList<>();					
					for (int i=0;i<nodes.getLength();i++){
						String title ="";						
						title = nodes.item(i).getTextContent();						
						productNames.add(title);
					}
					

					int selectedIndex = super.selectName(productNames, name);
					
					if (selectedIndex>=0){
						Node selectedNode = nodes.item(selectedIndex);
						Node parent = selectedNode.getParentNode();
						if (parent!=null){
							NamedNodeMap attributes = parent.getAttributes();
							if (attributes!=null){
								for (int i=0;i<attributes.getLength();i++){
									Node attribute = attributes.item(i);
									if ("objectid".equals(attribute.getNodeName())){
										reference = attribute.getNodeValue();
										break;
									}
								}
							}
						}
					}
					
				}
				
			}
		}
		
		return reference;
	}
	
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

		if (reference==null && product.getName()!=null){
			reference = getIdFromName(product.getName());			
		}
		if (reference == null){
			return null;
		}
		String url = null;
		
		url = this.ENDPOINTv2;		
		url = url + "?stats=1";		
		url = url + "&id=" + reference;
		logger.info("BoardgameGeek url for fetch data: " + url);
		
		return super.fetchDocFromUrl(url);
		
	}

	@Override
	public byte[] getImageData(Document doc) throws TooFastConnectionException {
		byte[] image = null;
		String imageUrl = super.getField(doc, imageUrlDocPath);
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
		return super.getField(doc, itemDescriptionDocPath);
	}

	@Override
	public Integer getPublicationYear(Document doc) throws TooFastConnectionException {
		Integer year = null;
		String yearString = super.getAttribute(doc, yearPublishedDocPath, yearPublishedAttribute);
		if (yearString!=null){
			try {
				year = Integer.parseInt(yearString);
			}catch (Exception e){
				logger.error("Publisher year is not integer", e);
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
    public String getExternalUrlLink(Document doc) throws TooFastConnectionException{
		String reference = this.getReference(doc);
		String url = null;
		if (reference!=null){
			url = PRODUCT_URL + reference + "/";
		} 
		return url;
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
		String ratingString = super.getAttribute(doc, ratingDocPath,ratingAttribute);		
		if (ratingString!=null){
			try{
				rating = Double.parseDouble(ratingString);
			}catch (Exception e){
				logger.error("Rating is not a double", e);
			}
		}
		
		return rating;
	}
	
	public String getIdentifier(){
		return IDENTIFIER;
	}

	@Override
	public String getReference(Document doc) throws TooFastConnectionException {
		return super.getAttribute(doc, referenceDocPath, referenceAttribute);
	}


}
