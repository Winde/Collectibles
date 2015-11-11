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
import java.util.List;
import java.util.Map;

import model.connection.ProductInfoLookupServiceXML;
import model.connection.TooFastConnectionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

@Service
public class AmazonItemLookupService extends ProductInfoLookupServiceXML{

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
    	System.out.println("Amazon url for fetch data: " + url);
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
    
    public byte[] getImageData(Document doc) throws TooFastConnectionException{
    	byte [] data = null;
    	String url = parseImageUrl(doc);
    	if (url!=null){
    		data = fetchImage(url);
    	}
    	return data;
    }

	@Override
	public Document fetchDocFromId(String id) throws TooFastConnectionException, FileNotFoundException {
		String url = this.getMultipleUseUrl(id);
		return this.fetchDocFromUrl(url);
	}


	@Override
	public Integer getPublicationYear(Document doc)
			throws TooFastConnectionException {
		return null;
	}

	@Override
	public List<String> getAuthors(Document doc)
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
}
