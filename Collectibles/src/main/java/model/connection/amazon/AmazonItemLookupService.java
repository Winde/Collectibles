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
import java.util.HashMap;
import java.util.Map;
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

@Service
public class AmazonItemLookupService extends ProductInfoLookupServiceXML{

	private static final Logger logger = LoggerFactory.getLogger(AmazonItemLookupService.class);
	
	
    private String AWS_ACCESS_KEY_ID = null;
    private String AWS_SECRET_KEY = null;
    private String ENDPOINT = null;
    private String ASSOCIATE_TAG = null;
        
    @Autowired
    public AmazonItemLookupService(
    		@Value("${AWS_ACCESS_KEY_ID}")String AWS_ACCESS_KEY_ID,
    		@Value("${AWS_SECRET_KEY}")String AWS_SECRET_KEY,
    		@Value("${ENDPOINT}") String ENDPOINT,
    		@Value("${ASSOCIATE_TAG}")String ASSOCIATE_TAG){

    	this.AWS_SECRET_KEY = AWS_SECRET_KEY;
    	this.AWS_ACCESS_KEY_ID = AWS_ACCESS_KEY_ID;    	    
    	this.ENDPOINT = ENDPOINT;
	    this.ASSOCIATE_TAG = ASSOCIATE_TAG;
    }    
    
    private String getUrl(String service,String operation,String responseGroup, String id){
    	/*
         * Set up the signed requests helper 
         */
        SignedRequestsHelper helper;
        try {
            helper = SignedRequestsHelper.getInstance(ENDPOINT, AWS_ACCESS_KEY_ID, AWS_SECRET_KEY);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        
        String requestUrl = null;

        /* The helper can sign requests in two forms - map form and string form */
        
        /*
         * Here is an example in map form, where the request parameters are stored in a map.
         */        
        Map<String, String> params = new HashMap<String, String>();
        params.put("Service", "service");
        params.put("Version", "2009-03-31");
        params.put("Operation", operation);
        params.put("ItemId", id);
        params.put("ResponseGroup", responseGroup);
        params.put("AssociateTag", ASSOCIATE_TAG);

        requestUrl = helper.sign(params);
        
        return requestUrl;
    }
    
    public String getMultipleUseUrl(String id) throws TooFastConnectionException {
    	String url = getUrl("AWSECommerceService","ItemLookup","Large",id);
    	logger.info("Amazon url for fetch data: " + url);
    	return url;
    }

    public String getAmazonUrl(Document doc) throws TooFastConnectionException {
    	return this.getField(doc, "/ItemLookupResponse/Items/Item/DetailPageURL");		    	
	}
    
    public String getDescription(Document doc ) throws TooFastConnectionException{
    	return this.getField(doc, "/ItemLookupResponse/Items/Item/EditorialReviews/EditorialReview/Content");
    }
    
    private String parseImageUrl(Document doc) throws TooFastConnectionException{
    	return this.getField(doc, "/ItemLookupResponse/Items/Item/LargeImage/URL");       
    }        
    
	@Override
	public String getPublisher(Document doc) throws TooFastConnectionException {
		return this.getField(doc, "/ItemLookupResponse/Items/Item/ItemAttributes/Publisher");
	}
    
    public byte[] getImageData(Document doc) throws TooFastConnectionException{
    	byte [] data = null;
    	String url = parseImageUrl(doc);
    	if (url!=null){
    		data = fetchImage(url);
    	}
    	return data;
    }

	@Override
	public Document fetchDocFromProduct(Product product) throws TooFastConnectionException, FileNotFoundException {
		if (product.getUniversalReference()==null){
			return null;
		}
		String url = this.getMultipleUseUrl(product.getUniversalReference());
		return this.fetchDocFromUrl(url);
	}


	@Override
	public Integer getPublicationYear(Document doc)
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
	public String getGoodReadsUrl(Document doc)
			throws TooFastConnectionException {
		return null;
	}

	@Override
	public String getDrivethrurpgUrl(Document doc)
			throws TooFastConnectionException {
		return null;
	}

	@Override
	public Map<String, Long> getDollarPrice(Document doc) throws TooFastConnectionException {
		Map<String,Long> result = null;
		String newField = this.getField(doc, "/ItemLookupResponse/Items/Item/OfferSummary/LowestNewPrice/Amount");
		String usedField = this.getField(doc, "/ItemLookupResponse/Items/Item/OfferSummary/LowestUsedPrice/Amount");
		if (newField!=null || usedField!=null){
			result = new HashMap<>();
			if (newField!=null){
				Long newPrice = null;
				try {
					newPrice = Long.parseLong(newField);
				}catch(Exception ex) {ex.printStackTrace();}
				if (newPrice!=null){
					result.put("New", newPrice);
				}
			}
			if (usedField!=null){
				Long usedPrice = null;
				try {
					usedPrice = Long.parseLong(usedField);
				}catch(Exception ex) {ex.printStackTrace();}
				if (usedPrice!=null){
					result.put("Used", usedPrice);
				}
			}			
		}
		return result;
	}


}
