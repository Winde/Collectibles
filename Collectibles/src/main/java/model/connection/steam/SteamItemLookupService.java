package model.connection.steam;

import java.io.FileNotFoundException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import model.connection.AbstractProductInfoLookupService;
import model.connection.TooFastConnectionException;
import model.dataobjects.Author;
import model.dataobjects.Product;

import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class SteamItemLookupService extends AbstractProductInfoLookupService<JsonNode> {

	private static final Logger logger = LoggerFactory.getLogger(SteamItemLookupService.class);
	
	private static final int MAX_ADDITIONAL_IMAGES = 2;
	
	private String searchByNameEndpoint = null;
	private String searchByIdEndpoint = null;
	private String obtainOwnedEndpoint = null;
	private String key = null;
	
	
	@Autowired
	public SteamItemLookupService(
			@Value("${steam.searchbyname.endpoint}")String searchByNameEndpoint,
			@Value("${steam.searchbyid.endpoint}")String searchByIdEndpoint,
			@Value("${steam.secret.key}") String key,
			@Value("${steam.obtainowned.endpoint}") String obtainOwnedEndpoint) {
		this.searchByNameEndpoint = searchByNameEndpoint;
		this.searchByIdEndpoint = searchByIdEndpoint;
		this.obtainOwnedEndpoint = obtainOwnedEndpoint;
		this.key = key;
	}
	
	private String getReferenceByName(String name){
		String reference = null;
		org.jsoup.nodes.Document doc = null;			
		try {
			String url = searchByNameEndpoint;
			url = url + "?term=" + URLEncoder.encode(name,"UTF-8"); 
			url = url + "&f=games";
			
			logger.info("Connecting to url: " + url);
			doc = Jsoup.connect(url).get();
		} catch (Exception e) {
			logger.error("Issue when connecting to Steam HTML",e);
			e.printStackTrace();
		}
		if (doc!=null){
			Elements nameElements = doc.getElementsByClass("match_name");
			if (nameElements!=null){
				List<String> productNames = new ArrayList<>();			
				for (Element element : nameElements){
					String title = "";
					title = element.text();
					productNames.add(title);
				}
				
				int selectedIndex = super.selectName(productNames, name);
				if (selectedIndex>=0){
					Element selected = nameElements.get(selectedIndex);
					if (selected.parent()!=null){
						reference = selected.parent().attr("data-ds-appid");
					}
				}
			}
		}
		
		return reference;
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
	
	@Override
	public JsonNode fetchDocFromProduct(Product product) throws TooFastConnectionException, FileNotFoundException {
		JsonNode doc = null;
		String reference = this.getReferenceFromProduct(product);
		if (reference==null){			
			reference = this.getReferenceByName(product.getName());			
		}
		
		if (reference != null){			
					
			String url = searchByIdEndpoint+"?cc=us&appids="+reference;
						
			logger.info("Steam connecting to url: " + url);
			String json = null;
			try {
				URL urlObject = new URL(url);             
				json = IOUtils.toString(urlObject);
				if (json!=null){				
					ObjectMapper mapper = new ObjectMapper();					
					doc = mapper.readTree(json);
					if (doc!=null){
						doc = doc.path(reference);
					}
				}
			}catch (Exception e){
				logger.error("Issue obtaining data from steam",e);
			}
			
		}
		
		logger.info("Returning doc: " + doc);
		return doc;
	}

	@Override
	public byte[] getMainImageData(JsonNode doc) throws TooFastConnectionException {
		byte [] imageBytes = null;
		String imageUrl = doc.path("data").path("header_image").asText();
		if (imageUrl!=null && !"".equals(imageUrl.trim())){
			imageBytes = fetchImage(imageUrl);
		}
		return imageBytes;
	}

	@Override
	public String getDescription(JsonNode doc)
			throws TooFastConnectionException {
		return doc.path("data").path("detailed_description").asText();
	}

	@Override
	public Date getPublicationDate(JsonNode doc)
			throws TooFastConnectionException {
		Date releaseDate = null;
		String releaseDateString = doc.path("data").path("release_date").path("date").asText();
		if (releaseDateString!=null && !"".equals(releaseDateString)){
			SimpleDateFormat df = new SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH);
			try {
				releaseDate = df.parse(releaseDateString);
			} catch (ParseException e) {
				logger.error("Issue parsing date",e);				
			}
		}
		return releaseDate;
	}

	@Override
	public Set<Author> getAuthors(JsonNode doc) throws TooFastConnectionException {
		return null;
	}

	@Override
	public String getSeriesUrl(JsonNode doc) throws TooFastConnectionException { 
		return null;
	}

	@Override
	public String getExternalUrlLink(JsonNode doc) throws TooFastConnectionException {
		String link = null;
		String reference = this.getReference(doc);
		
		if (reference!=null){
			link = "http://store.steampowered.com/app/"+reference+"/";
		}
				
		return link;
	}

	@Override
	public String getPublisher(JsonNode doc) throws TooFastConnectionException {
		String publisher = null;
		Iterator<JsonNode> publishers = doc.path("data").path("publishers").elements();
		if (publishers!=null && publishers.hasNext()){
			publisher = publishers.next().asText();
		}
		return publisher;
	}
	

	@Override
	public Map<String, Long> getDollarPrice(JsonNode doc)
			throws TooFastConnectionException {
		Map<String,Long> map = null;
		Long price = null;
		String priceText = doc.path("data").path("price_overview").path("final").asText();
		if (priceText!=null && !"".equals(priceText)){
			try {
				price = Long.parseLong(priceText);
			}catch(Exception e){
				logger.error("Issue converting price to Long", e);
			}
		}
		if (price!=null){
			map = new HashMap<>();
			map.put("", price);
		}
		return map;
	}

	@Override
	public Double getRating(JsonNode doc) throws TooFastConnectionException {
		Double ratingOverTen = null;
		String ratingString = doc.path("data").path("metacritic").path("score").asText();
		if (ratingString!=null && !"".equals(ratingString)){
			Long ratingOverHundred = null;
			try {
				ratingOverHundred = Long.parseLong(ratingString);
			}catch(Exception e){
				logger.error("Issue converting rating to Long", e);
			}
			if (ratingOverHundred!=null){
				ratingOverTen = ratingOverHundred / 10.0;
			}
		}
		return ratingOverTen;
	}

	@Override
	public String getReference(JsonNode doc) throws TooFastConnectionException {
		String reference = doc.path("data").path("steam_appid").asText();
		return reference;
	}


	public String getIdentifier() {
		return "Steam";
	}

	@Override
	public List<byte[]> getAdditionalImageData(JsonNode doc) throws TooFastConnectionException {
		List<byte []> imageList = null;
		
		Iterator<JsonNode> imageNodes = doc.path("data").path("screenshots").elements();		
		if (imageNodes!=null){
			imageList = new ArrayList<>();
			int i=0;
			while (imageNodes.hasNext() && i<MAX_ADDITIONAL_IMAGES){
				JsonNode imageNode = imageNodes.next();
				String url = imageNode.path("path_full").asText();
				byte[] imageData = null;
				if (url!=null){
					imageData = super.fetchImage(url);
				}
				
				if (imageData!=null){
					imageList.add(imageData);
					i++;
				}				
			}
		}
		
		if (imageList!=null && imageList.isEmpty()){
			imageList = null;
		}
		return imageList;
	}

	@Override
	public List<String> getOwnedReferences(String userId) {
		List<String> references = null;
		String url = obtainOwnedEndpoint + "?key=" + key + "&steamid=" + userId + "&format=json";
		String json = null;
		try {
			URL urlObject = new URL(url);
			 json = IOUtils.toString(urlObject);
		} catch(Exception e){
			logger.error("Exception when obtained owned Steam games",e);
		}
		
		if (json!=null){
			ObjectMapper mapper = new ObjectMapper();
			JsonNode root = null;
			try {
				root = mapper.readTree(json);
			} catch (Exception e) {
				logger.error("Issue reading json", e);
			}
			 if (root!=null){
				 Iterator<JsonNode> gameNodes = root.path("response").path("games").elements();
				 if (gameNodes!=null){
					 references = new ArrayList<>();
					 while (gameNodes.hasNext()){
						JsonNode gameNode = gameNodes.next(); 
						String reference = gameNode.path("appid").asText();
						if (reference!=null){
							 references.add(reference);
						}							
					 }
				 }
			 }			
		}
		return references;
	}

	@Override
	public String getName(JsonNode doc) throws TooFastConnectionException {
		return doc.path("data").path("name").asText();
	}
}
