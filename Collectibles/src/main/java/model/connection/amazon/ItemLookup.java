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

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.client.HttpResponseException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


public class ItemLookup {

	private static ItemLookup instance = null;
	
    private String AWS_ACCESS_KEY_ID = null;
    private String AWS_SECRET_KEY = null;
    private String ENDPOINT = null;
    private String ASSOCIATE_TAG = null;
    
    
    private ItemLookup(){
    	Properties prop = new Properties();
    	String propFileName = "amazon.properties";
    	InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(propFileName);
    	if (resourceAsStream!=null){
    		try {
				prop.load(resourceAsStream);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		
    		AWS_SECRET_KEY = prop.getProperty("AWS_SECRET_KEY");
    		AWS_ACCESS_KEY_ID = prop.getProperty("AWS_ACCESS_KEY_ID");    	    
    	    ENDPOINT = prop.getProperty("ENDPOINT");
    	    ASSOCIATE_TAG = prop.getProperty("ASSOCIATE_TAG");
    	}
    }
    
    public static ItemLookup getInstance(){
    	if (instance == null){
    		instance = new ItemLookup();
    	} 
    	return instance;
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
    
    public String getDescription(String id) throws TooFastConnectionException {
    	String url = getUrl("AWSECommerceService","ItemLookup","Large",id);
    	System.out.println("Fecthing url: " + url);
    	return fetchDescription(url);
    }
    
    public String getImage(String id) throws TooFastConnectionException {        
    	String url = getUrl("AWSECommerceService","ItemLookup","Images",id);
    	System.out.println("Fecthing url: " + url);
        return fetchImage(url);
    }
    
	public String getAmazonUrl(String id) throws TooFastConnectionException {
		String url = getUrl("AWSECommerceService","ItemLookup","Large",id);
		System.out.println("Fecthing url: " + url);
		return fetchAmazonUrl(url);
	}


	private Document fetchDoc(String requestUrl) throws TooFastConnectionException{
    	
    	DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = null;
		try {
			db = dbf.newDocumentBuilder();
		} catch (ParserConfigurationException e1) {
			e1.printStackTrace();
			return null;
		}
        Document doc = null;
        try {
        	doc = db.parse(requestUrl);
        }catch (HttpResponseException hre) {
        	hre.printStackTrace();
		   if (hre.getStatusCode() == 503) {
			   throw new TooFastConnectionException();
		   }			
    	} catch (IOException ioe) {
    		ioe.printStackTrace();
        	return null;
        } catch (SAXException saxe) {
        	saxe.printStackTrace();
			return null;
		}
        return doc;
    }
	
    private String fetchAmazonUrl(String requestUrl) throws TooFastConnectionException {
    	String amazonUrl = null;
    	Document doc = fetchDoc(requestUrl);
    	if (doc!=null){
    		NodeList nodes = doc.getElementsByTagName("DetailPageURL");
    		if (nodes!=null && nodes.getLength()>0){
    			amazonUrl = nodes.item(0).getTextContent();
    		}
    	}
    	return amazonUrl;
	}

    
    private String fetchDescription(String requestUrl) throws TooFastConnectionException{
    	String description = null;
    	Document doc = fetchDoc(requestUrl);
    	if (doc!=null){
    		NodeList nodes = doc.getElementsByTagName("EditorialReviews");
    		if (nodes!=null && nodes.getLength()>0){
    			Element element = ((Element) nodes.item(0));
    			NodeList contentNodes = element.getElementsByTagName("Content");
    			if (contentNodes!=null && contentNodes.getLength()>0){
    				description = contentNodes.item(0).getTextContent();
	        	}    			
    		}
    	}
    	return description;    	
    }
    
    private String fetchImage(String requestUrl) throws TooFastConnectionException{
        String image = null;
        Document doc = fetchDoc(requestUrl);
        if (doc!=null){
	        NodeList nodes = doc.getElementsByTagName("LargeImage");
	        if (nodes!=null && nodes.getLength()>0){
	        	Element element = ((Element) nodes.item(0));
	        	NodeList urlNodes = element.getElementsByTagName("URL");
	        	if (urlNodes!=null && urlNodes.getLength()>0){
	        		image = urlNodes.item(0).getTextContent();
	        	}
	        	
	        }    
        }
        return image;
    }



}
