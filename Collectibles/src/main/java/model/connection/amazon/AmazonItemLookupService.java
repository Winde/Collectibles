/**********************************************************************************************
 * Copyright 2009 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file 
 * except in compliance with the License. A copy of the License is located at
 *
 *       http://aws.amazon.com/apache2.0/
 *
 * or in the "LICENSE.txt" file accompanying this file. This file is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under the License. 
 *
 * ********************************************************************************************
 *
 *  Amazon Product Advertising API
 *  Signed Requests Sample Code
 *
 *  API Version: 2009-03-31
 *
 */

package model.connection.amazon;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import model.connection.ProductInfoLookupServiceXML;
import model.connection.TooFastConnectionException;
import model.dataobjects.Author;
import model.dataobjects.Product;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@Service
public class AmazonItemLookupService extends ProductInfoLookupServiceXML{

	private static final Logger logger = LoggerFactory.getLogger(AmazonItemLookupService.class);
	private static final String IDENTIFIER = "Amazon";	
	
	private String AWS_ACCESS_KEY_ID = null;
	private String AWS_SECRET_KEY = null;
	private String PROTOCOL = null;
	private String ENDPOINT = null;
	private String ASSOCIATE_TAG = null;
	private String VERSION = null;
	private String SERVICE = null;

	private final String externalLinkUrlDocPath = "/ItemLookupResponse/Items/Item/DetailPageURL";
	private final String descriptionDocPath = "/ItemLookupResponse/Items/Item/EditorialReviews/EditorialReview/Content";
	private final String imageUrlDocPath = "/ItemLookupResponse/Items/Item/LargeImage/URL";
	private final String publisherDocPath = "/ItemLookupResponse/Items/Item/ItemAttributes/Publisher";
	private final String dollarPriceNewPath = "/ItemLookupResponse/Items/Item/OfferSummary/LowestNewPrice/Amount";
	private final String dollarPriceUsedPath = "/ItemLookupResponse/Items/Item/OfferSummary/LowestUsedPrice/Amount";
	private final String dollarPriceCollectiblePath = "/ItemLookupResponse/Items/Item/OfferSummary/LowestCollectiblePrice/Amount";
	private final String amazonReferencePath = "/ItemLookupResponse/Items/Item/ASIN";
	
	private final int sleepTimeBetwenCheckByNameAndCheckById = 500;
	
	
	
	private void sleepDueToAmazonMaxChecksPerMinute(){
		try {
			Thread.sleep(sleepTimeBetwenCheckByNameAndCheckById);
		} catch (InterruptedException e) {					
			logger.error("Buffer sleep interrupted", e);
		}
	}
	
	@Autowired
	public AmazonItemLookupService(
			@Value("${AMAZON.AWS_ACCESS_KEY_ID}")String AWS_ACCESS_KEY_ID,
			@Value("${AMAZON.AWS_SECRET_KEY}")String AWS_SECRET_KEY,
			@Value("${AMAZON.PROTOCOL}") String PROTOCOL,
			@Value("${AMAZON.ENDPOINT}") String ENDPOINT,
			@Value("${AMAZON.ASSOCIATE_TAG}")String ASSOCIATE_TAG,
			@Value("${AMAZON.SERVICE_VERSION}") String VERSION,
			@Value("${AMAZON.SERVICE}") String SERVICE ){

		this.AWS_SECRET_KEY = AWS_SECRET_KEY;
		this.AWS_ACCESS_KEY_ID = AWS_ACCESS_KEY_ID;
		this.PROTOCOL  = PROTOCOL;
		this.ENDPOINT = ENDPOINT;
		this.ASSOCIATE_TAG = ASSOCIATE_TAG;
		this.VERSION = VERSION;
		this.SERVICE = SERVICE;
	}    

	private String getReferenceByName(String name) {
		String reference = null;
		String url = this.getSearchByName(name);
		if (url!=null){
			Document doc = null;
			try {
				doc = super.fetchDocFromUrl(url);
			} catch (FileNotFoundException | TooFastConnectionException e) {				
				logger.error("Issue connecting to Amazon by searching by name", e);
			}
			if (doc!=null){
				NodeList nodes = super.getNodes(doc, "/ItemSearchResponse/Items/Item/ItemAttributes/Title");
				if (nodes!=null){
					List<String> productNames = new ArrayList<>();
					for (int i=0;i<nodes.getLength();i++){
						String title = "";
						title = nodes.item(i).getTextContent();
						productNames.add(title);
					}
					
					int selectedIndex = super.selectName(productNames, name);
					if (selectedIndex>=0){
						Node selectedNode = nodes.item(selectedIndex);
						if (selectedNode!=null && selectedNode.getParentNode()!=null && selectedNode.getParentNode().getParentNode()!=null){
							NodeList dataNodes = selectedNode.getParentNode().getParentNode().getChildNodes();
							if (dataNodes!=null){
								for (int i=0;i<dataNodes.getLength();i++){
									if ("ASIN".equals(dataNodes.item(i).getNodeName())){
										reference = dataNodes.item(i).getTextContent();
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
    
	private String getSearchByNameUrl(String operation,String searchIndex, String responseGroup, String title){
		/*
		Set up the signed requests helper 
		 */
		SignedRequestsHelper helper;
		try {
			helper = SignedRequestsHelper.getInstance(PROTOCOL,ENDPOINT, AWS_ACCESS_KEY_ID, AWS_SECRET_KEY);
		} catch (Exception e) {
			logger.error("Can't create Amazon signer helper", e);
			return null;
		}
        
		String requestUrl = null;

		/* The helper can sign requests in two forms - map form and string form */
        
		/*
		 * Here is an example in map form, where the request parameters are stored in a map.
		 */        
		Map<String, String> params = new HashMap<String, String>();
		params.put("Service", SERVICE);
		params.put("Version", VERSION);
		params.put("Operation", operation);
		params.put("Keywords", title);
		params.put("SearchIndex", searchIndex);        
		params.put("ResponseGroup", responseGroup);
		params.put("AssociateTag", ASSOCIATE_TAG);

		requestUrl = helper.sign(params);
        
		return requestUrl;

	}
    
	private String getUrl(String operation,String responseGroup, String id){
		/*
		 * Set up the signed requests helper 
		 */
		SignedRequestsHelper helper;
		try {
			helper = SignedRequestsHelper.getInstance(PROTOCOL,ENDPOINT, AWS_ACCESS_KEY_ID, AWS_SECRET_KEY);
		} catch (Exception e) {
			logger.error("Can't create Amazon signer helper", e);
			return null;
		}

		String requestUrl = null;

		/* The helper can sign requests in two forms - map form and string form */
        
		/*
		 * Here is an example in map form, where the request parameters are stored in a map.
		 */        
		Map<String, String> params = new HashMap<String, String>();
		params.put("Service", SERVICE);
		params.put("Version", VERSION);
		params.put("Operation", operation);
		params.put("ItemId", id);
		params.put("ResponseGroup", responseGroup);
		params.put("AssociateTag", ASSOCIATE_TAG);

		requestUrl = helper.sign(params);
        
		return requestUrl;
    }
    
	public String getMultipleUseUrl(String id) {
		String url = getUrl("ItemLookup","Large",id);
		logger.info("Amazon url for fetch data: " + url);
		return url;
	}
	
	public String getSearchByName(String name) {
		String url = getSearchByNameUrl("ItemSearch","All","Small",name);
		logger.info("Amazon url for fetch data by name: " + url);
		return url;
	}

	@Override
	public String getExternalUrlLink(Document doc){
		return this.getField(doc, externalLinkUrlDocPath);		    	
	}
    
	public String getDescription(Document doc ) throws TooFastConnectionException{
		return this.getField(doc, descriptionDocPath);
	}
    
	private String parseImageUrl(Document doc) throws TooFastConnectionException{
		return this.getField(doc, imageUrlDocPath);       
	}
    
	@Override
	public String getPublisher(Document doc) throws TooFastConnectionException {
		return this.getField(doc, publisherDocPath);
	}
    
	public byte[] getMainImageData(Document doc) throws TooFastConnectionException{
		byte [] data = null;
		String url = parseImageUrl(doc);
		if (url!=null){
			data = fetchImage(url);
		}
		return data;
	}
	
	@Override
	public Document fetchDocFromProduct(Product product) throws TooFastConnectionException, FileNotFoundException {		
		String reference = null;
		reference = this.getReferenceFromProduct(product);
		if (reference==null){			
			reference = this.getReferenceByName(product.getName());
			if (reference!=null){
				sleepDueToAmazonMaxChecksPerMinute();
			}
		}
		
		if (reference == null){
			return null;
		}
		String url = this.getMultipleUseUrl(reference);
		return this.fetchDocFromUrl(url);
	}

	@Override
	public Date getPublicationDate(Document doc)
			throws TooFastConnectionException {
		return null;
	}

	@Override
	public Set<Author> getAuthors(Document doc)
			throws TooFastConnectionException {
		return null;
	}

	@Override
	public String getSeriesUrl(Document doc) throws TooFastConnectionException {
		return null;
	}

	@Override
	public Map<String, Long> getDollarPrice(Document doc) throws TooFastConnectionException {
		Map<String,Long> result = null;
		Map<String,String> stringPrices = new HashMap<>();		
		String newField = this.getField(doc, dollarPriceNewPath);
		String usedField = this.getField(doc, dollarPriceUsedPath);
		String collectibleField = this.getField(doc, dollarPriceCollectiblePath);
		if (newField!=null) { stringPrices.put("New", newField); }
		if (usedField!=null) { stringPrices.put("Used", usedField); }
		if (collectibleField!=null) { stringPrices.put("Collectible", collectibleField); }
		if (stringPrices!=null && !stringPrices.isEmpty()){
			result = new HashMap<>();
			for (Entry<String,String> entry : stringPrices.entrySet()){
				Long price = null;
				try {
					price = Long.parseLong(entry.getValue());
				}catch (Exception e){
					logger.error("Amazon price is not long? ", e);
				}
				if (price!=null){
					result.put(entry.getKey(), price);
				}
			}			
		}
		return result;
	}


	public String getIdentifier(){
		return IDENTIFIER;
	}

	@Override
	public Double getRating(Document doc) throws TooFastConnectionException {
		return null;
	}

	@Override
	public String getReference(Document doc) throws TooFastConnectionException {
		return super.getField(doc, amazonReferencePath);
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

}
