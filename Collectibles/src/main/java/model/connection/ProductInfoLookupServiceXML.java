package model.connection;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public abstract class ProductInfoLookupServiceXML implements ProductInfoLookupService<Document> {

	protected byte[] fetchImage(String url) throws TooFastConnectionException {
		
		if (url!=null){
			URL imageURL;
			try {
				imageURL = new URL(url);
			} catch (MalformedURLException e) {				
				e.printStackTrace();
				return null;
			}
		    BufferedImage originalImage = null;
			try {
				originalImage = ImageIO.read(imageURL);
			 } catch (IOException ioe) {	    		
	    		if (ioe.getMessage()!=null && ioe.getMessage().contains("HTTP response code: 503")){
	    			throw new TooFastConnectionException();	
	    		} 
	    		ioe.printStackTrace();
	        	return null;
			}
			if (originalImage==null){
				return null;
			}
		    ByteArrayOutputStream baos=new ByteArrayOutputStream();				
			try {
				ImageIO.write(originalImage, "jpg", baos );
			} catch (IOException e) {				
				e.printStackTrace();
				return null;
			}
			byte[] imageInByte=baos.toByteArray();			
			return imageInByte;
		}
		 
		return null;
	}
	
	protected Document fetchDocFromUrl(String requestUrl) throws TooFastConnectionException,FileNotFoundException {
    	
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
        } catch (IOException ioe) {
    		ioe.printStackTrace();
    		if (ioe.getMessage()!=null && ioe.getMessage().contains("HTTP response code: 503")){    			
    			throw new TooFastConnectionException();    				
    		}    		
        	return null;
        } catch (SAXException saxe) {
        	saxe.printStackTrace();
			return null;
		}
        return doc;
    }
	
	protected String getField(Document doc,String xpath){
		String field = null;
		XPath xPath = XPathFactory.newInstance().newXPath();	
		try {
			NodeList nodes = (NodeList)xPath.evaluate(xpath, doc.getDocumentElement(),XPathConstants.NODESET);
			if (nodes!=null && nodes.getLength()>0){
				field = nodes.item(0).getTextContent();
			}
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception ex){
			ex.printStackTrace();
		}
		
		return field;
	}
	
	protected List<String> getFields(Document doc,String xpath){
		List<String> fields = null;
		XPath xPath = XPathFactory.newInstance().newXPath();	
		try {
			NodeList nodes = (NodeList)xPath.evaluate(xpath, doc.getDocumentElement(),XPathConstants.NODESET);
			if (nodes!=null && nodes.getLength()>0){
				fields = new ArrayList<>();
				for (int i=0;i<nodes.getLength();i++){
					Node node = nodes.item(i);
					fields.add(node.getTextContent());
				}
			}
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception ex){
			ex.printStackTrace();
		}
		
		return fields;
	}
	
}
