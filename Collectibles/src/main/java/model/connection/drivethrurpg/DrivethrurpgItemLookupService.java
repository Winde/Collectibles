package model.connection.drivethrurpg;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import model.connection.ProductInfoLookupService;
import model.connection.TooFastConnectionException;
import model.dataobjects.Product;

import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class DrivethrurpgItemLookupService implements ProductInfoLookupService<DrivethrurpgData> {


	private static final Logger logger = LoggerFactory.getLogger(DrivethrurpgItemLookupService.class);
	
	private static final int MAXIMUM_DISTANCE_TO_CONSIDER_MULTIPLE_RESULTS = 8;		
	private static final int MAXIMUM_DISTANCE_TO_CONSIDER_SINGLE_RESULTS = 15;
	
	private Element getListing(Product product, Document doc){
		
		Element result = null;
		
		
		return result;
	}
	
	public String fetchUrlFromProductName(Product product) throws TooFastConnectionException {
		String link = null;
		String url = null;
		try {
			if (product.getName()!=null){
				url = "http://www.drivethrurpg.com/browse.php?keywords=";
				url = url + URLEncoder.encode(product.getName(),"UTF-8");
				
				logger.info("Connecting to url: " + url);				
				Document doc = Jsoup.connect(url).get();
				if (doc!=null){
					logger.info("Obtained doc");
					Elements productTables = doc.getElementsByClass("productListing");
				
					if (productTables!=null && productTables.size()>0){
						logger.info("Obtained productTables");
						
						Element productTable = productTables.get(0);
						
						if (productTable!=null){
							logger.info("Obtained productTable");
							Elements productListings = productTable.getElementsByTag("tr");

								
							//First line is header					
							List<String> productNames = new ArrayList<>();
							if (productListings!=null && productListings.size()>1){
								
								logger.info("Have productListings");
								
								for (int i=1;i<productListings.size();i++){
									String title ="";
									Element productListing = productListings.get(i);
									Element titleContainer = productListing.getElementsByTag("h1").first();
									
									if (titleContainer!=null){
										title = titleContainer.text();
									}
									productNames.add(title);
								}
								
								int selectedIndex = -1;
								int minDistance = -1;
								for (int i=0;i<productNames.size();i++){
									if (productNames.get(i)!=null && !"".equals(productNames.get(i).trim())){
										
										int distance = StringUtils.getLevenshteinDistance(product.getName(), productNames.get(i));
										if (product.getHierarchyPlacement()!=null && product.getHierarchyPlacement().getName()!=null){
											int distanceWithHierarchy = StringUtils.getLevenshteinDistance(product.getHierarchyPlacement().getName() + ": " + product.getName(), productNames.get(i));
											distance = Math.min(distance, distanceWithHierarchy);
										}
										
										logger.info("Title to discriminate: " + productNames.get(i) + ", distance= " + distance);
										
										if (distance < minDistance || minDistance < 0){
											logger.info("Could be current best");
											logger.info("Distance < maximum for multiple? " + (distance <= MAXIMUM_DISTANCE_TO_CONSIDER_MULTIPLE_RESULTS));
											logger.info("Distance < maximum for single? " + (distance <= MAXIMUM_DISTANCE_TO_CONSIDER_SINGLE_RESULTS));
											logger.info("We have X results =  " + productNames.size());
											if (
												(distance <= MAXIMUM_DISTANCE_TO_CONSIDER_MULTIPLE_RESULTS)
												||
												(distance <= MAXIMUM_DISTANCE_TO_CONSIDER_SINGLE_RESULTS && productNames.size()==1)
											){
											minDistance = distance;
											selectedIndex = i;
											}																																
										}						
									}					
								}
								
		
								if (selectedIndex>=0){
									logger.info("Selected: " + productNames.get(selectedIndex));
									Element selected = productListings.get(selectedIndex +1);
									
									
									if (selected!=null){	
										Elements links = selected.getElementsByTag("a");
										for (Element linkTag : links) {
											String href = linkTag.attr("href");
											if (href!=null && href.startsWith("http://www.drivethrurpg.com/product/")){
												link = href;
												break;
											}
										}
									}
									
									if (link!=null){
										
										if (link.indexOf("?")>0){
											link = link + "&";
										} else {
											link = link + "?";
										}
										link = link +  "affiliate_id=597859";			
									}
									
									logger.info("Found link: " + link);																		
								}
								
								
							}
						}		
					}
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return link;
	
	}
	
	@Override
	public DrivethrurpgData fetchDocFromProduct(Product product) throws TooFastConnectionException {
		DrivethrurpgData data = new DrivethrurpgData();
		if (product.getDrivethrurpgUrl()!=null && !"".equals(product.getDrivethrurpgUrl().trim())){
			data.setLink(product.getDrivethrurpgUrl());
		} else {
			data.setLink(this.fetchUrlFromProductName(product));
		}
		if (data.getLink()!=null){
			try {
				Document doc = Jsoup.connect(data.getLink()).get();
				if (doc!=null){
					data.setDoc(doc);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return data;
	}

	@Override
	public byte[] getImageData(DrivethrurpgData doc) throws TooFastConnectionException {
		return null;
	}

	@Override
	public String getDescription(DrivethrurpgData doc) throws TooFastConnectionException {
		return null;
	}

	@Override
	public Integer getPublicationYear(DrivethrurpgData doc) throws TooFastConnectionException {
		return null;
	}

	@Override
	public Set getAuthors(DrivethrurpgData doc) throws TooFastConnectionException {
		return null;
	}

	@Override
	public String getSeriesUrl(DrivethrurpgData doc) throws TooFastConnectionException {
		return null;
	}

	@Override
	public String getAmazonUrl(DrivethrurpgData doc) throws TooFastConnectionException {
		return null;
	}


	@Override
	public String getGoodReadsUrl(DrivethrurpgData doc) throws TooFastConnectionException {
		return null;
	}

	@Override
	public String getPublisher(DrivethrurpgData doc) throws TooFastConnectionException {
		return null;
	}

	@Override
	public String getDrivethrurpgUrl(DrivethrurpgData doc) throws TooFastConnectionException {
		String link = null;
		if (doc!=null){
			link = doc.getLink();
		}
		return link;
	}
	
	private Long getPriceFromProductPriceNode(Element element){
		String priceText = null;
		Long priceLong = null;
		if (element!=null){
			Elements priceElements = null;
			priceElements = element.getElementsByClass("product-price-special");
			if (priceElements==null || priceElements.size()<=0){
				priceElements = element.getElementsByClass("product-price-base");
			}
			if (priceElements!=null && priceElements.size()>0){
				priceText = priceElements.text();  
			}
			if (priceText!=null){
				priceText = priceText.replaceAll("\\.", "");
				priceText = priceText.replaceAll("\\$", "");
				priceText = priceText.replaceAll("\u00A0", "");
				priceText = priceText.trim();					
				try{
					priceLong = Long.parseLong(priceText.replaceAll("\\.", ""));				
				}catch(Exception ex){ex.printStackTrace();}
			}
		}
				
		return priceLong;
	}
	
	public Map<String,Long> getDollarPrice(DrivethrurpgData doc) throws TooFastConnectionException{
		Long priceLong = null;
		String priceString = null;
		Map<String,Long> result = null;
		if (doc!=null && doc.getDoc()!=null){
			
			Elements priceEntries = doc.getDoc().getElementsByClass("product-price-item");
			

			Element hardcoverColorPremiumNode = null;
			Element hardcoverPremiumNode = null;
			Element hardcoverNode = null;
			Element softcoverColorPremiumNode = null;
			Element softcoverPremiumNode = null;
			Element softcoverNode = null;
			
			Long hardcoverColorPremiumPrice = null;
			Long hardcoverPremiumPrice = null;
			Long hardcoverPrice = null;
			Long softcoverColorPremiumPrice = null;
			Long softcoverPremiumPrice = null;
			Long softcoverPrice = null;
			
			
			if (priceEntries!=null){
				for (int i=0;i<priceEntries.size();i++){
					Element priceEntry = priceEntries.get(i);
					String text = priceEntry.text();
					if (text!=null){
						
						Long price = getPriceFromProductPriceNode(priceEntry);
						logger.info("Text: " + text);
						
						if (text.contains("Hardcover") && text.contains("Premium") && text.contains("Color")){
							if (price!=null && (hardcoverColorPremiumPrice==null || price <hardcoverColorPremiumPrice)){
								hardcoverColorPremiumNode = priceEntry;
								hardcoverColorPremiumPrice = price;								
							}
							logger.info("Price Hardcover Premium Color: " + price);
						}else if (text.contains("Hardcover") && text.contains("Premium")){
							if (price!=null && (hardcoverPremiumPrice==null || price <hardcoverPremiumPrice)){
								hardcoverPremiumNode = priceEntry;
								hardcoverPremiumPrice = price;
							}
							logger.info("Price Hardcover Premium: " + price);
						} else if (text.contains("Softcover") && text.contains("Premium") && text.contains("Color")){
							if (price!=null && (softcoverColorPremiumPrice==null || price <softcoverColorPremiumPrice)){
								softcoverColorPremiumNode = priceEntry;
								softcoverColorPremiumPrice = price;
							}
							logger.info("Price Softcover Premium Color: " + price);
						} else if (text.contains("Softcover") && text.contains("Premium")){
							if (price!=null && (softcoverPremiumPrice==null || price <softcoverPremiumPrice)){
								softcoverPremiumNode = priceEntry;
								softcoverPremiumPrice = price;
							}
							logger.info("Price Softcover Premium: " + price);
						} else if (text.contains("Hardcover")){
							if (price!=null && (hardcoverPrice==null || price <hardcoverPrice)){
								hardcoverNode = priceEntry;
								hardcoverPrice = price;
							}						
							logger.info("Price Hardcover: " + price);
						} else if (text.contains("Softcover")){
							if (price!=null && (softcoverPrice==null || price <softcoverPrice)){
								softcoverNode = priceEntry;
								softcoverPrice = price;
							}	
							logger.info("Price Softcover: " + price);
						}
					}
				}
				
				result = new HashMap<>();
				if (hardcoverColorPremiumPrice!=null) { 
					result.put("Hardcover Premium Color", hardcoverColorPremiumPrice);
				}
				if (hardcoverPremiumPrice!=null) {
					result.put("Hardcover Premium", hardcoverPremiumPrice);					
				}
				if (hardcoverPrice!=null) {
					result.put("Hardcover", hardcoverPrice);					
				}
				if (softcoverColorPremiumPrice!=null) {
					result.put("Softcover Premium Color", softcoverColorPremiumPrice);					
				}
				if (softcoverPremiumPrice!=null) {
					result.put("Softcover Premium", softcoverPremiumPrice);					
				}
				if (softcoverPrice!=null) {
					result.put("Softcover", softcoverPrice);
				}
				if (result.isEmpty()){
					result = null;
				}
			}
			
		}

		return result;
	}

}
