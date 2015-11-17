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
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@Service
public class BoardGameGeekLookupService extends ProductInfoLookupServiceXML {

	private static final Logger logger = LoggerFactory.getLogger(BoardGameGeekLookupService.class);
	

	private static final String IDENTIFIER = "BoardGameGeek";
		
	private String getIdFromName(String name){
		String reference = null;
		String url = null;
		try {
			url = "https://www.boardgamegeek.com/xmlapi/search?search="+URLEncoder.encode(name, "UTF-8");			
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (url!=null){
			Document doc = null;
			 try {
				 doc = super.fetchDocFromUrl(url);
			} catch (FileNotFoundException | TooFastConnectionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			 
			if (doc!=null){
				NodeList nodes = super.getNodes(doc, "/boardgames/boardgame/name");
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
		
		url = "https://www.boardgamegeek.com/xmlapi2/thing?stats=1";		
		url = url + "&id=" + reference;
		logger.info("BoardgameGeek url for fetch data: " + url);
		
		return super.fetchDocFromUrl(url);
		
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

	@Override
	public String getReference(Document doc) throws TooFastConnectionException {
		return super.getAttribute(doc, "/items/item", "id");
	}


}
