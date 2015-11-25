package model.connection.boardgamegeek;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import model.connection.ProductInfoLookupServiceXML;
import model.connection.TooFastConnectionException;
import model.connection.amazon.AmazonItemLookupService;
import model.dataobjects.Author;
import model.dataobjects.Product;
import model.dataobjects.Rating;

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
	
	private String DEFAULT_PRODUCT_URL = null;
	private String BOARDGAME_PRODUCT_URL = null;
	private String RPG_PRODUCT_URL = null;	
	private String ENDPOINT = null;
	private String OPERATION_THING = null;
	private String OPERATION_SEARCH = null;
	private String OPERATION_SEARCH_PARAMETER = null;
	private String OPERATION_THING_PARAMETER = null;
	
	private final long RATING_PRIORITY = 10;
	
	private final String gameNameDocPath = "/items/item/name";
	private final String imageUrlDocPath = "/items/item/image";
	private final String itemDescriptionDocPath = "/items/item/description";
	
	private final String yearPublishedDocPath = "/items/item/yearpublished";
	private final String yearPublishedAttribute = "value";
	
	private final String ratingDocPathBayesAverage = "/items/item/statistics/ratings/bayesaverage";
	private final String ratingDocPathAverage = "/items/item/statistics/ratings/average";
	private final String ratingDocPathCount = "/items/item/statistics/ratings/usersrated";
	private final String ratingAttribute = "value";	
	private final String ratingCountAttribute = "value";
	
	
	private final String referenceDocPath = "/items/item";
	private final String referenceAttribute = "id";
	
	private final String typeDocPath = "/items/item";
	private final String typeAttribute = "type";

		
	@Autowired
	public BoardGameGeekLookupService(			
			@Value("${BOARDGAMEGEEK.V2.ENDPOINTv2}")String ENDPOINT,
			@Value("${BOARDGAMEGEEK.V2.OPERATION.SEARCH}")String OPERATION_SEARCH,
			@Value("${BOARDGAMEGEEK.V2.OPERATION.SEARCH.PARAMETER}")String OPERATION_SEARCH_PARAMETER,
			@Value("${BOARDGAMEGEEK.V2.OPERATION.THING}")String OPERATION_THING,			
			@Value("${BOARDGAMEGEEK.V2.OPERATION.THING.PARAMETER}")String OPERATION_THING_PARAMETER,
			@Value("${BOARDGAMEGEEK.DEFAULT_PRODUCT_URL}")String DEFAULT_PRODUCT_URL,
			@Value("${BOARDGAMEGEEK.BOARDGAME_PRODUCT_URL}")String BOARDGAME_PRODUCT_URL,
			@Value("${BOARDGAMEGEEK.RPG_PRODUCT_URL}")String RPG_PRODUCT_URL){		
		this.ENDPOINT = ENDPOINT;
		this.OPERATION_SEARCH = OPERATION_SEARCH;
		this.OPERATION_SEARCH_PARAMETER = OPERATION_SEARCH_PARAMETER;
		this.OPERATION_THING = OPERATION_THING;
		this.OPERATION_THING_PARAMETER = OPERATION_THING_PARAMETER;
		this.DEFAULT_PRODUCT_URL = DEFAULT_PRODUCT_URL;	
		this.BOARDGAME_PRODUCT_URL = BOARDGAME_PRODUCT_URL;
		this.RPG_PRODUCT_URL = RPG_PRODUCT_URL;
	}
	
	private String getIdFromName(String name){
		String reference = null;
		String url = null;
		try {
			url = this.ENDPOINT +this.OPERATION_SEARCH+"?"+this.OPERATION_SEARCH_PARAMETER+"="+URLEncoder.encode(name, "UTF-8");			
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
						NamedNodeMap attributes = nodes.item(i).getAttributes();
						for (int j=0;j<attributes.getLength();j++){
							Node attribute = attributes.item(j);
							if ("value".equals(attribute.getNodeName())){
								title = attribute.getNodeValue();
								break;
							}
						}
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
									if ("id".equals(attribute.getNodeName())){
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
		String reference = this.getReferenceFromProduct(product);
		
		if (reference==null && product.getName()!=null){
			reference = getIdFromName(product.getName());			
		}
		if (reference == null){
			return null;
		}
		String url = null;
		
		url = this.ENDPOINT;	
		url = url + this.OPERATION_THING;
		url = url + "?stats=1";		
		url = url + "&"+this.OPERATION_THING_PARAMETER+"=" + reference;
		logger.info("BoardgameGeek url for fetch data: " + url);
		
		return super.fetchDocFromUrl(url);
		
	}

	@Override
	public byte[] getMainImageData(Document doc) throws TooFastConnectionException {
		byte[] image = null;
		String imageUrl = super.getField(doc, imageUrlDocPath);
		if (imageUrl!=null) {
			if (imageUrl.startsWith("//")){
				imageUrl = "http:"+imageUrl;
			}
			logger.info(this.getIdentifier() + " Image url: " + imageUrl);
			image = super.fetchImage(imageUrl);
		}
		return image;
	}

	@Override
	public String getDescription(Document doc) throws TooFastConnectionException {
		return super.getField(doc, itemDescriptionDocPath);
	}

	@Override
	public Date getPublicationDate(Document doc) throws TooFastConnectionException {
		Date date = null;
		Integer year = null;
		String yearString = super.getAttribute(doc, yearPublishedDocPath, yearPublishedAttribute);
		if (yearString!=null){
			try {
				year = Integer.parseInt(yearString);
				
				date = super.getDateFromYear(year);
				
			}catch (Exception e){
				logger.error("Publisher year is not integer", e);
			}
		}
		return date;
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
			url = DEFAULT_PRODUCT_URL + reference + "/";
			String typeString = super.getAttribute(doc, typeDocPath,typeAttribute);		
			if (typeString!=null){
				if ("boardgame".equals(typeString)){
					url = BOARDGAME_PRODUCT_URL + reference + "/";
				} else if ("rpgitem".equals(typeString)){
					url = RPG_PRODUCT_URL + reference + "/";
				}
			}
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
	public Rating getRating(Document doc) throws TooFastConnectionException {
		Double rating = null;
		Long ratingCount = null;
		String ratingString = super.getAttribute(doc, ratingDocPathBayesAverage,ratingAttribute);
		String ratingCountString = super.getAttribute(doc, ratingDocPathCount,ratingCountAttribute);		
		if (ratingString!=null){
			try{
				rating = Double.parseDouble(ratingString);
			}catch (Exception e){
				logger.error("Rating is not a double", e);
			}
		}
		if (rating==null || rating<=0.0){
			ratingString = super.getAttribute(doc, ratingDocPathAverage,ratingAttribute);
			try{
				rating = Double.parseDouble(ratingString);
			}catch (Exception e){
				logger.error("Rating is not a double", e);
			}
		}
		if (ratingCountString!=null && !"".equals(ratingCountString)){
			try {
				ratingCount = Long.parseLong(ratingCountString);
			}catch (Exception ex){
				ex.printStackTrace();
			}
			
		}
		
		
		if (rating!=null && rating<=0.0){
			rating = null;
		}				
		
		Rating ratingObject = null;
		if (rating!=null){
			ratingObject = new Rating();
			ratingObject.setPriority(RATING_PRIORITY);
			ratingObject.setProvider(this.getIdentifier());
			ratingObject.setRatingsCount(ratingCount);
			ratingObject.setRating(rating);
		}
		return ratingObject;
	}
	
	public String getIdentifier(){
		return IDENTIFIER;
	}

	@Override
	public String getReference(Document doc) throws TooFastConnectionException {
		return super.getAttribute(doc, referenceDocPath, referenceAttribute);
	}

	@Override
	public List<byte[]> getAdditionalImageData(Document doc)
			throws TooFastConnectionException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName(Document doc) throws TooFastConnectionException {
		// TODO Auto-generated method stub
		return null;
	}

	public String getReferenceFromProduct(Product product){
		String reference = null;
		if (product.getConnectorReferences()!=null){
			reference = product.getConnectorReferences().get(this.getIdentifier());
		}
		if (reference!=null && "".equals(reference.trim())){
			reference = null;
		}
		return reference;
	}

}
