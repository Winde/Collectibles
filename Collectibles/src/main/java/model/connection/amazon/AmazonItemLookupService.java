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
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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
	
	private String getUrl(String operation, Map<String,String> params){
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
		Map<String, String> webParams = new HashMap<String, String>();
		webParams.put("Service", SERVICE);
		webParams.put("Version", VERSION);
		webParams.put("Operation", operation);		
		webParams.put("AssociateTag", ASSOCIATE_TAG);

		for (Entry<String, String> param: params.entrySet()){
			webParams.put(param.getKey(), param.getValue());
		}
		
		requestUrl = helper.sign(webParams);
        
		return requestUrl;

	}
    
	public String getMultipleUseUrl(String id) {
		Map<String,String> params = new HashMap<>();
		params.put("ItemId", id);
		params.put("ResponseGroup", "OfferFull,Large");
		
		
		//String url = getUrl("ItemLookup","Large",id);		
		String url = getUrl("ItemLookup",params);
		logger.info("Amazon url for fetch data: " + url);
		return url;
	}
	
	public String getSearchByName(String name) {
		Map<String,String> params = new HashMap<>();
		params.put("Keywords", name);
		params.put("SearchIndex", "All");        
		params.put("ResponseGroup", "Small");
		
		String url = getUrl("ItemSearch",params);
		//String url = getSearchByNameUrl("ItemSearch","All","Small",name);
		logger.info("Amazon url for fetch data by name: " + url);
		return url;
	}

	
	@Override
	public String getExternalUrlLink(Node node){
		return this.getField(node, externalLinkUrlDocPath);		    	
	}
    
	public String getDescription(Node node) throws TooFastConnectionException{
		return this.getField(node, descriptionDocPath);
	}
    
	private String parseImageUrl(Node node) throws TooFastConnectionException{
		return this.getField(node, imageUrlDocPath);       
	}
    
	@Override
	public String getPublisher(Node node) throws TooFastConnectionException {
		return this.getField(node, publisherDocPath);
	}
    
	public byte[] getMainImageData(Node node) throws TooFastConnectionException{
		byte [] data = null;
		String url = parseImageUrl(node);
		if (url!=null){
			data = fetchImage(url);
		}
		return data;
	}
	
	@Override
	public Node fetchDocFromProduct(Product product) throws TooFastConnectionException, FileNotFoundException {		
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
		Node node = null;
		Document doc = this.fetchDocFromUrl(url);
		if (doc!=null){
			node = doc.getDocumentElement();
		}
		return node;
	}

	@Override
	public Date getPublicationDate(Node node) throws TooFastConnectionException {
		return null;
	}

	@Override
	public Set<Author> getAuthors(Node node) throws TooFastConnectionException {
		return null;
	}

	@Override
	public String getSeriesUrl(Node node) throws TooFastConnectionException {
		return null;
	}
	
	
	private String getMerchant(Node conditionNode){
		String name = null;
		if (conditionNode!=null){
			Node offerNode = conditionNode.getParentNode().getParentNode();
			name = super.getField(offerNode, "Merchant/Name");
		}
		return name;
	}

	@Override
	public Collection<Price> getPrices(Node node) throws TooFastConnectionException {
		Collection<Price> result = null;
		Map<String,String> stringPrices = new HashMap<>();		
		String newField = this.getField(node, dollarPriceNewPath);
		String usedField = this.getField(node, dollarPriceUsedPath);
		String collectibleField = this.getField(node, dollarPriceCollectiblePath);
		if (newField!=null) { stringPrices.put("New", newField); }
		if (usedField!=null) { stringPrices.put("Used", usedField); }
		if (collectibleField!=null) { stringPrices.put("Collectible", collectibleField); }
		
		NodeList merchantNodesCondition = super.getNodes(node, "/ItemLookupResponse/Items/Item/Offers/Offer/OfferAttributes/Condition");

		if (stringPrices!=null && !stringPrices.isEmpty()){
			result = new ArrayList<>();
			for (Entry<String,String> entry : stringPrices.entrySet()){
				Long price = null;
				try {
					price = Long.parseLong(entry.getValue());
				}catch (Exception e){
					logger.error("Amazon price is not long? ", e);
				}
				if (price!=null){
					Price priceObject = new Price();
					priceObject.setConnectorName(this.getIdentifier());
					priceObject.setLink(this.getExternalUrlLink(node));
					priceObject.setType(entry.getKey());
					priceObject.setPrice(price);
					if (merchantNodesCondition!=null) {
						for (int i=0;i<merchantNodesCondition.getLength();i++){
							Node currentMerchant = merchantNodesCondition.item(i);
							if (entry.getKey().equals(currentMerchant.getTextContent())){
								priceObject.setSeller(getMerchant(currentMerchant));
								break;
							}
						}
					}
					
					logger.info("PriceObject adding: " + priceObject);				
					result.add(priceObject);
				}
			}			
		}
		return result;
	}


	public String getIdentifier(){
		return IDENTIFIER;
	}

	@Override
	public Rating getRating(Node node) throws TooFastConnectionException {
		return null;
	}

	@Override
	public String getReference(Node node) throws TooFastConnectionException {
		return super.getField(node, amazonReferencePath);
	}

	@Override
	public List<byte[]> getAdditionalImageData(Node node) throws TooFastConnectionException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName(Node node) throws TooFastConnectionException {
		// TODO Auto-generated method stub
		return null;
	}

}
