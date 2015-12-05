package model.connection.ebay;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import model.connection.ProductInfoLookupServiceXML;
import model.connection.TooFastConnectionException;
import model.dataobjects.Author;
import model.dataobjects.Price;
import model.dataobjects.Product;
import model.dataobjects.Rating;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

@Service
public class EbayInfoLookupService extends ProductInfoLookupServiceXML {

	private static final Logger logger = LoggerFactory.getLogger(EbayInfoLookupService.class);
	
	private String ENDPOINT = null;
	private String OPERATION = null;
	private String VERSION = null;
	private String SECRET = null;
	private String RESPONSE_DATA_FORMAT = null;
	private String ENTRIES_PER_PAGE = null;
	
	@Autowired
	public EbayInfoLookupService(
			@Value("${EBAY.ENDPOINT}")String ENDPOINT,
			@Value("${EBAY.OPERATION_NAME}")String OPERATION,
			@Value("${EBAY.VERSION}")String VERSION,
			@Value("${EBAY.APPID}")String SECRET,
			@Value("${EBAY.RESPONSE_DATA_FORMAT}")String RESPONSE_DATA_FORMAT,
			@Value("${EBAY.ENTRIES_PER_PAGE}")String ENTRIES_PER_PAGE){
		this.ENDPOINT = ENDPOINT;
		this.OPERATION = OPERATION;
		this.VERSION = VERSION;
		this.SECRET = SECRET;
		this.RESPONSE_DATA_FORMAT = RESPONSE_DATA_FORMAT;
		this.ENTRIES_PER_PAGE = ENTRIES_PER_PAGE;
		
	}
		
	protected String getField(Node node, String path){
		String field = null;
		XPath xPath = XPathFactory.newInstance().newXPath();	
		try {
			NodeList nodes = (NodeList)xPath.evaluate(path, node,XPathConstants.NODESET);
			if (nodes!=null && nodes.getLength()>0){
				field = nodes.item(0).getTextContent();
			}
		} catch (Exception e){
			logger.error("Exception obtaining field from XPath", e);
		}
		
		return field;
	}
	
	protected Document fetchDocFromUrl(String requestUrl) throws TooFastConnectionException,FileNotFoundException {
    	
    	DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = null;
		try {
			db = dbf.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			logger.error("Exception for document parser", e);
			return null;
		}
        Document doc = null;
        try {
        	doc = db.parse(requestUrl);
        } catch (IOException ioe) {
        	logger.error("Exception reaching out to XML through internet", ioe);
    		if (ioe.getMessage()!=null && ioe.getMessage().contains("HTTP response code: 503")){    			
    			throw new TooFastConnectionException();    				
    		}    		
        	return null;
        } catch (SAXException saxe) {
        	logger.error("SAXE exception", saxe);
			return null;
		}
        return doc;
    }
	
	@Override
	public Node fetchDocFromProduct(Product product) throws TooFastConnectionException, FileNotFoundException {
		String productId = null;
		String productType = null;
		
		if (product.getConnectorReferences()!=null && product.getConnectorReferences().get(this.getIdentifier())!=null){
			productId = product.getConnectorReferences().get(this.getIdentifier());
			productType = "ReferenceID";
		} 
		if (productId==null || "".equals(productId.trim())){
			productId = product.getUniversalReference();
			productType = "ISBN";
		}
		
		String url = null;
		if (productId!=null){		
			url = ENDPOINT + "?" +
				"OPERATION-NAME=" + OPERATION + "&" +
				"SERVICE-VERSION=" + VERSION + "&" +
				"SECURITY-APPNAME=" + SECRET + "&" +
				"RESPONSE-DATA-FORMAT=" + RESPONSE_DATA_FORMAT + "&" +
				"REST-PAYLOAD" + "&" +
				"paginationInput.entriesPerPage=" + ENTRIES_PER_PAGE + "&" +
				"productId.@type=" + productType + "&"+
				"productId=" + productId + "&" + 
				"outputSelector=" + "SellerInfo";
				
		}
		logger.info(this.getIdentifier() + " connecting to url: " + url);
		Node result = null;
		Document doc = null;
		if (url!=null){
			doc = this.fetchDocFromUrl(url);
		}
		if (doc!=null){
			result = selectMinPrice(doc);
		}
 		return result;
	}

	@Override
	public byte[] getMainImageData(Node node) throws TooFastConnectionException {
		return null;
	}

	@Override
	public List<byte[]> getAdditionalImageData(Node node)
			throws TooFastConnectionException {
		return null;
	}

	@Override
	public String getDescription(Node node)
			throws TooFastConnectionException {
		return null;
	}

	@Override
	public Date getPublicationDate(Node node)
			throws TooFastConnectionException {
		return null;
	}

	@Override
	public Set<Author> getAuthors(Node node)
			throws TooFastConnectionException {
		return null;
	}

	@Override
	public String getSeriesUrl(Node node) throws TooFastConnectionException {
		return null;
	}

	private Node selectMinPrice(Document doc){
		NodeList nodes = null;
		XPath xPath = XPathFactory.newInstance().newXPath();	
		try {
			nodes = (NodeList)xPath.evaluate("/findItemsByProductResponse/searchResult/item/sellingStatus/convertedCurrentPrice", doc.getDocumentElement(),XPathConstants.NODESET);
		}catch(Exception ex){
			logger.error("Issue parsing XML", ex);			
		}
			
		
		Double minPrice = null;
		Node selectedNode = null;
		if (nodes!=null && nodes.getLength()>0){
			logger.info(this.getIdentifier() + " Found " + nodes.getLength() +" nodes");
			for (int i=0;i<nodes.getLength();i++){
				Node node = nodes.item(i);
				Double nodePrice = null;
				String nodePriceString = node.getTextContent();
				logger.info(this.getIdentifier() + " Node price string: " + nodePriceString);
				if (nodePriceString!=null && !"".equals(nodePriceString.trim())){
					try {
						nodePrice = Double.parseDouble(nodePriceString);
					}catch (Exception ex){
						logger.error("Error converting price",ex);
					}
				}
				if (nodePrice!=null && (minPrice==null || nodePrice<minPrice)){
					selectedNode = node;
					minPrice = nodePrice;
				}
			}			
		} else {
			logger.info(this.getIdentifier() + " Found NO nodes");
		}
		
		Node result = null;
		if (selectedNode!=null){
			result = selectedNode.getParentNode().getParentNode();
		}
		
		logger.info(this.getIdentifier() + " Selected node:" + result);
		return result;
	}
	
	@Override
	public String getExternalUrlLink(Node node) throws TooFastConnectionException {				
		//logger.info(this.getIdentifier() + " found url: " + link);
		//return link;
		return null;
	}

	@Override
	public String getPublisher(Node node) throws TooFastConnectionException {
		return null;
	}

	@Override
	public Collection<Price> getPrices(Node node) throws TooFastConnectionException {
		String sellerString = super.getField(node,"sellerInfo/sellerUserName");
		String priceString = super.getField(node,"sellingStatus/convertedCurrentPrice");
		String link = super.getField(node,"viewItemURL");
		logger.debug(this.getIdentifier() + " PriceString: " + priceString);
		Long price = null;
		if (priceString!=null && !"".equals(priceString.trim())){
			try {
				price = new Double(Double.parseDouble(priceString)*100).longValue();
			}catch(Exception ex){
				logger.error("Issue converting price",ex);
			}
		}
		Collection<Price> result = null;
		if (price!=null){
			result = new ArrayList<>();
			Price priceObject = new Price();
			priceObject.setConnectorName(this.getIdentifier());
			priceObject.setLink(link);
			priceObject.setCurrency("USD");
			priceObject.setPrice(price);
			priceObject.setType("");
			priceObject.setSeller(sellerString);
			result.add(priceObject);
		}
		
		return result;
	}

	@Override
	public Rating getRating(Node node) throws TooFastConnectionException {
		return null;
	}

	@Override
	public String getReference(Node node) throws TooFastConnectionException {
		return super.getField(node,"/item/productId");		
	}

	@Override
	public String getName(Node node) throws TooFastConnectionException {
		return null;
	}

	@Override
	public String getIdentifier() {
		return "Ebay";
	}

}
