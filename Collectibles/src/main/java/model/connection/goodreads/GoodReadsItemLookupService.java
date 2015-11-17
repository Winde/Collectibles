package model.connection.goodreads;

import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import model.connection.ProductInfoLookupServiceXML;
import model.connection.TooFastConnectionException;
import model.connection.drivethrurpg.DrivethrurpgData;
import model.dataobjects.Author;
import model.dataobjects.Product;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@Service
public class GoodReadsItemLookupService extends ProductInfoLookupServiceXML {

	private static final Logger logger = LoggerFactory.getLogger(GoodReadsItemLookupService.class);
		
	private static final String IDENTIFIER = "Goodreads";
	
	private String key = null;
	private String entryPointQueryOne = null;
	private String baseUrlSeries = null;
	
	@Autowired
	public GoodReadsItemLookupService(
			@Value("${goodreads.public.key}") String key,
			@Value("${goodreads.entryPoint.query.isbn}") String entryPointQueryOne,
			@Value("${goodreads.url.series}") String baseUrlSeries) {
		this.key = key;
		this.entryPointQueryOne = entryPointQueryOne;
		this.baseUrlSeries  = baseUrlSeries;
	}
	
	public String getLookupUrl(String id){
		String url = entryPointQueryOne + "?isbn="+id+"&key="+key;
		logger.info("Goodreads url for fetch data: " + url);
		return url;
	}
			
	public String getImageUrl(Document doc){		
		return this.getField(doc, "/GoodreadsResponse/book/image_url");		
	}
	
	public byte [] getImageData(Document doc) throws TooFastConnectionException{
		byte [] data = null;
		String url = this.getImageUrl(doc);
		if (url!=null && url.indexOf("/nophoto/")<0){
			data = this.fetchImage(url);
		}
		return data;
	}
	
	public String getDescription(Document doc){
		return this.getField(doc, "/GoodreadsResponse/book/description");		
	}
	
	public Integer getPublicationYear(Document doc){
		Integer result = null;
		try{
			result = Integer.parseInt(this.getField(doc, "/GoodreadsResponse/book/work/original_publication_year"));
		}catch (Exception ex){
			ex.printStackTrace();
		}
		return result;
	}
	
	@Override
	 public String getExternalUrlLink(Document doc) throws TooFastConnectionException {
		return this.getField(doc, "/GoodreadsResponse/book/url");
	}
	
	public Set<Author> getAuthors(Document doc){
		Set<Author> authors = new HashSet<>();
		NodeList authorNodes = this.getNodes(doc, "/GoodreadsResponse/book/authors/author");
		if (authorNodes!=null && authorNodes.getLength()>0){
			for (int i=0;i<authorNodes.getLength();i++){
				Node node = authorNodes.item(i);
				Element element = (Element) node;
				String id =  (element.getElementsByTagName("id").getLength()>0 ? element.getElementsByTagName("id").item(0).getTextContent() : null);
				String name = (element.getElementsByTagName("name").getLength()>0 ? element.getElementsByTagName("name").item(0).getTextContent() : null);
				String imageUrl = null;
				Node imageElement = null; 
				if (element.getElementsByTagName("image_url").getLength()>0 ){
					boolean validImage = true;
					imageElement = element.getElementsByTagName("image_url").item(0);
					NamedNodeMap attributes = imageElement.getAttributes();
					if (attributes!=null && attributes.getLength()>0){
						for (int j=0;j<attributes.getLength();j++){
							Node attribute = attributes.item(j);
							if ("nophoto".equals(attribute.getNodeName()) && "true".equals(attribute.getNodeValue())){
								validImage = false;
							}							
						}
					}
					if (validImage){
						imageUrl = imageElement.getTextContent();
					}
				}
				String link = (element.getElementsByTagName("link").getLength()>0 ? element.getElementsByTagName("link").item(0).getTextContent() : null);
				byte [] byteImage = null;
				if (imageUrl!=null){
					try {
						byteImage = fetchImage(imageUrl);
					} catch (TooFastConnectionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				if (imageUrl!=null){
					imageUrl = imageUrl.replaceAll("\\n", "");
				}
				link = link.replaceAll("\\n", "");
				if (id!=null){					
					Author author = new Author();
					author.setId(id);
					author.setName(name);
					author.setImageData(byteImage);
					author.setGoodreadsAuthorLink(link);
					authors.add(author);
				}				
			}
		}
		if (authors.size()>0){
			return authors;
		} else {
			return null;
		}
	}
	
	public String getSeriesUrl(Document doc){
		String url = null;		
		String series = this.getField(doc, "/GoodreadsResponse/book/series_works/series_work/series/id");
		if (series !=null) {
			url = baseUrlSeries + series;
		}
		return url;		
	}
	
	@Override
	public String getPublisher(Document doc) throws TooFastConnectionException {
		return this.getField(doc, "/GoodreadsResponse/book/publisher");
	}

	@Override
	public Document fetchDocFromProduct(Product product) throws TooFastConnectionException {
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
		String requestUrl = null;
		
		
		requestUrl = this.getLookupUrl(reference);
		
		logger.info(this.getIdentifier()+ "connecting to url: " + requestUrl);
		Document doc = null;
		try{
			doc = this.fetchDocFromUrl(requestUrl);
		}catch(FileNotFoundException ex){
			ex.printStackTrace();
			return null;
		}
		return doc;
	}

	@Override
	public Map<String,Long> getDollarPrice(Document doc) throws TooFastConnectionException {
		return null;
	}


	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public Double getRating(Document doc) throws TooFastConnectionException {
		return null;
	}

	
	
}
