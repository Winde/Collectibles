package model.connection.drivethrurpg;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
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
public class DrivethrurpgItemLookupService implements ProductInfoLookupService<Element> {


	private static final Logger logger = LoggerFactory.getLogger(DrivethrurpgItemLookupService.class);
	
	private static final int MAXIMUM_DISTANCE_TO_CONSIDER_MULTIPLE_RESULTS = 8;		
	private static final int MAXIMUM_DISTANCE_TO_CONSIDER_SINGLE_RESULTS = 15;
	
	private Element getListing(Product product, Document doc){
		
		Element result = null;
		
		
		return result;
	}
	
	@Override
	public Element fetchDocFromProduct(Product product) throws TooFastConnectionException {
		Element result = null;
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
									result = productListings.get(selectedIndex +1);
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
		return result;
	}

	@Override
	public byte[] getImageData(Element doc) throws TooFastConnectionException {
		return null;
	}

	@Override
	public String getDescription(Element doc) throws TooFastConnectionException {
		return null;
	}

	@Override
	public Integer getPublicationYear(Element doc) throws TooFastConnectionException {
		return null;
	}

	@Override
	public Set getAuthors(Element doc) throws TooFastConnectionException {
		return null;
	}

	@Override
	public String getSeriesUrl(Element doc) throws TooFastConnectionException {
		return null;
	}

	@Override
	public String getAmazonUrl(Element doc) throws TooFastConnectionException {
		return null;
	}


	@Override
	public String getGoodReadsUrl(Element doc) throws TooFastConnectionException {
		return null;
	}

	@Override
	public String getPublisher(Element doc) throws TooFastConnectionException {
		return null;
	}

	@Override
	public String getDrivethrurpgUrl(Element productListing) throws TooFastConnectionException {
						
		String link = null;
		if (productListing!=null){	
			Elements links = productListing.getElementsByTag("a");
			for (Element linkTag : links) {
				String href = linkTag.attr("href");
				if (href!=null && href.startsWith("http://www.drivethrurpg.com/product/")){
					link = href;
					break;
				}
			}
		}
		
		logger.info("Found link: " + link);
		return link;
	}
	
	public Long getDollarPrice(Element productListing) throws TooFastConnectionException{
		Long priceLong = null;
		String priceString = null;
		if (productListing!=null){	
			
			Elements productSepcialPriceNode = productListing.getElementsByClass("productSpecialPrice");
			if (productSepcialPriceNode!=null && productSepcialPriceNode.size()==1){
				priceString = productSepcialPriceNode.get(0).text();
				
			}else {
				Element lastColumn = productListing.getElementsByTag("td").last();
				priceString = lastColumn.ownText();
			}
		}
		try {
			if (priceString!=null){
				priceString = priceString.replaceAll("\\.", "");
				priceString = priceString.replaceAll("\\$", "");
				priceString = priceString.replaceAll("\u00A0", "");
				priceString = priceString.trim();
				logger.info("Price string: *" + priceString+"*");			
				priceLong = Long.parseLong(priceString.replaceAll("\\.", ""));
			}
		}catch(Exception ex){ ex.printStackTrace();}
		return priceLong;
	}

}
