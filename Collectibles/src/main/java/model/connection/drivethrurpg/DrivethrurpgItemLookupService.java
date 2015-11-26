package model.connection.drivethrurpg;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import model.connection.AbstractProductInfoLookupService;
import model.connection.ProductInfoLookupService;
import model.connection.TooFastConnectionException;
import model.dataobjects.Price;
import model.dataobjects.Product;
import model.dataobjects.Rating;

import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class DrivethrurpgItemLookupService extends AbstractProductInfoLookupService<DrivethrurpgData> {


	private static final Logger logger = LoggerFactory.getLogger(DrivethrurpgItemLookupService.class);
	
	private static final String IDENTIFIER = "DrivethruRPG";

	
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
				
				logger.debug("Connecting to url: " + url);				
				Document doc = Jsoup.connect(url).get();
				if (doc!=null){
					logger.debug("Obtained doc");
					Elements productTables = doc.getElementsByClass("productListing");
				
					if (productTables!=null && productTables.size()>0){
						logger.debug("Obtained productTables");
						
						Element productTable = productTables.get(0);
						
						if (productTable!=null){
							logger.debug("Obtained productTable");
							Elements productListings = productTable.getElementsByTag("tr");

								
							//First line is header					
							List<String> productNames = new ArrayList<>();
							if (productListings!=null && productListings.size()>1){
								
								logger.debug("Have productListings");
								
								for (int i=1;i<productListings.size();i++){
									String title ="";
									Element productListing = productListings.get(i);
									Element titleContainer = productListing.getElementsByTag("h1").first();
									
									if (titleContainer!=null){
										title = titleContainer.text();
									}
									productNames.add(title);
								}
								
								int selectedIndex = super.selectName(productNames,product.getName());
								
		
								if (selectedIndex>=0){
									logger.debug("Selected: " + productNames.get(selectedIndex));
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
									
									logger.debug("Found link: " + link);																		
								}
								
								
							}
						}		
					}
				}
			}
		} catch (IOException e) {
			logger.error("Issue when fetching info for drivethrurpg from product name", e);
		}
		return link;
	
	}
	
	@Override
	public DrivethrurpgData fetchDocFromProduct(Product product) throws TooFastConnectionException {
		DrivethrurpgData data = new DrivethrurpgData();
		String link = null;
		if (product.getExternalLinks()!=null){
			link = product.getExternalLinks().get(this.getIdentifier());
		}
		if (link!=null && !"".equals(link.trim())){
			data.setLink(product.getExternalLinks().get(this.getIdentifier()));
		} else {
			data.setLink(this.fetchUrlFromProductName(product));
		}
		if (data.getLink()!=null){
			try {
				Document doc = Jsoup.connect(data.getLink()).get();
				if (doc!=null){
					data.setDoc(doc);
				} else {
					data = null;
				}
			} catch (IOException e) {
				logger.error("Issue when fetching product page from drivethrurpg", e);
			}
		}
		
		return data;
	}

	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public byte[] getMainImageData(DrivethrurpgData doc) throws TooFastConnectionException {
		return null;
	}

	@Override
	public String getDescription(DrivethrurpgData doc) throws TooFastConnectionException {
		return null;
	}

	@Override
	public Date getPublicationDate(DrivethrurpgData doc) throws TooFastConnectionException {
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
	public String getPublisher(DrivethrurpgData doc) throws TooFastConnectionException {
		return null;
	}

	@Override
    public String getExternalUrlLink(DrivethrurpgData doc) throws TooFastConnectionException {
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
				}catch(Exception e){					
					logger.error("Price "+priceText+" is not long?", e);					
				}
			}
		}
				
		return priceLong;
	}
	
	private Price createPrice(String key, String link, Long priceAmount){
		Price price = new Price();
		price.setConnectorName(this.getIdentifier());
		price.setLink(link);
		price.setPrice(priceAmount);
		price.setType(key);
		return price;
	}
	
	public Collection<Price> getPrices(DrivethrurpgData doc) throws TooFastConnectionException{
		Long priceLong = null;
		String priceString = null;
		Collection<Price> result = null;
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
						logger.debug("Text: " + text);
						
						if (text.contains("Hardcover") && text.contains("Premium") && text.contains("Color")){
							if (price!=null && (hardcoverColorPremiumPrice==null || price <hardcoverColorPremiumPrice)){
								hardcoverColorPremiumNode = priceEntry;
								hardcoverColorPremiumPrice = price;								
							}
							logger.debug("Price Hardcover Premium Color: " + price);
						}else if (text.contains("Hardcover") && text.contains("Premium")){
							if (price!=null && (hardcoverPremiumPrice==null || price <hardcoverPremiumPrice)){
								hardcoverPremiumNode = priceEntry;
								hardcoverPremiumPrice = price;
							}
							logger.debug("Price Hardcover Premium: " + price);
						} else if (text.contains("Softcover") && text.contains("Premium") && text.contains("Color")){
							if (price!=null && (softcoverColorPremiumPrice==null || price <softcoverColorPremiumPrice)){
								softcoverColorPremiumNode = priceEntry;
								softcoverColorPremiumPrice = price;
							}
							logger.debug("Price Softcover Premium Color: " + price);
						} else if (text.contains("Softcover") && text.contains("Premium")){
							if (price!=null && (softcoverPremiumPrice==null || price <softcoverPremiumPrice)){
								softcoverPremiumNode = priceEntry;
								softcoverPremiumPrice = price;
							}
							logger.debug("Price Softcover Premium: " + price);
						} else if (text.contains("Hardcover")){
							if (price!=null && (hardcoverPrice==null || price <hardcoverPrice)){
								hardcoverNode = priceEntry;
								hardcoverPrice = price;
							}						
							logger.debug("Price Hardcover: " + price);
						} else if (text.contains("Softcover")){
							if (price!=null && (softcoverPrice==null || price <softcoverPrice)){
								softcoverNode = priceEntry;
								softcoverPrice = price;
							}	
							logger.debug("Price Softcover: " + price);
						}
					}
				}
				
				String link = this.getExternalUrlLink(doc);
				result = new ArrayList<>();
				Price price = null;
				if (hardcoverColorPremiumPrice!=null) {
					price = createPrice("Hardcover Premium Color",link,hardcoverColorPremiumPrice);
					result.add(price);
				}
				if (hardcoverPremiumPrice!=null) {					
					price = createPrice("Hardcover Premium",link,hardcoverPremiumPrice);
					result.add(price);
				}
				if (hardcoverPrice!=null) {
					price = createPrice("Hardcover",link,hardcoverPrice);
					result.add(price);		
				}
				if (softcoverColorPremiumPrice!=null) {
					price = createPrice("Softcover Premium Color",link,softcoverColorPremiumPrice);
					result.add(price);		
				}
				if (softcoverPremiumPrice!=null) {
					price = createPrice("Softcover Premium",link,softcoverPremiumPrice);
					result.add(price);		
				}
				if (softcoverPrice!=null) {
					price = createPrice("Softcover",link,softcoverPrice);
					result.add(price);		
				}
				if (result.isEmpty()){
					result = null;
				}
			}
			
		}

		return result;
	}

	@Override
	public Rating getRating(DrivethrurpgData doc)
			throws TooFastConnectionException {
		return null;
	}

	@Override
	public String getReference(DrivethrurpgData doc)
			throws TooFastConnectionException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<byte[]> getAdditionalImageData(DrivethrurpgData doc)
			throws TooFastConnectionException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName(DrivethrurpgData doc) throws TooFastConnectionException {
		// TODO Auto-generated method stub
		return null;
	}

}
