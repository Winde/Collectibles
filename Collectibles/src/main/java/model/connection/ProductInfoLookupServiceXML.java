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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public abstract class ProductInfoLookupServiceXML extends AbstractProductInfoLookupService<Document> {

	private static final Logger logger = LoggerFactory.getLogger(ProductInfoLookupServiceXML.class);	
	
	protected byte[] fetchImage(String url) throws TooFastConnectionException {
		byte[] imageInByte = null;
		if (url!=null){
			URL imageURL = null;	
			try {
				imageURL = new URL(url);
			} catch (MalformedURLException e) {				
				e.printStackTrace();				
			}
			if (imageURL!=null) {
			    BufferedImage originalImage = null;
				try {
					originalImage = ImageIO.read(imageURL);
				 } catch (IOException ioe) {	    		
		    		if (ioe.getMessage()!=null && ioe.getMessage().contains("HTTP response code: 503")){
		    			throw new TooFastConnectionException();	
		    		} 
		    		ioe.printStackTrace();		        	
				}
				if (originalImage!=null){	
					boolean writeError = false;
				    ByteArrayOutputStream baos=new ByteArrayOutputStream();				
					try {
						ImageIO.write(originalImage, "jpg", baos );
					} catch (IOException e) {				
						e.printStackTrace();	
						writeError = true;
					}
					if (!writeError){
						imageInByte=baos.toByteArray();
					}
				}
			}
		}
		 
		return imageInByte;
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
	
	protected NodeList getNodes(Document doc,String xpath){
		NodeList nodes = null;
		XPath xPath = XPathFactory.newInstance().newXPath();	
		try {
			nodes = (NodeList)xPath.evaluate(xpath, doc.getDocumentElement(),XPathConstants.NODESET);
			
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception ex){
			ex.printStackTrace();
		}
		
		return nodes;
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
	
	public String getAttribute(Document doc, String xpath, String attributeToReturn) {
		String result = null;
		XPath xPath = XPathFactory.newInstance().newXPath();			
		try {
			NodeList nodes = (NodeList)xPath.evaluate(xpath, doc.getDocumentElement(),XPathConstants.NODESET);
			if (nodes!=null && nodes.getLength()>0) {
				for (int i=0;i<nodes.getLength();i++){
					Node node = nodes.item(i);
					NamedNodeMap attributes = node.getAttributes();
					logger.info("Node: " +node );
					if (attributes!=null && attributes.getLength()>0) {
						for (int j=0;j<attributes.getLength();j++) {							
							Node attribute = attributes.item(j);	
							logger.info("Attribute: " +attribute + "[name: " + attribute.getNodeName() + ", value: " + attribute.getNodeValue() + "]" );
							logger.info(attributeToReturn + "=" + attribute.getNodeName() +"? " + (attribute!=null && attributeToReturn.equals(attribute.getNodeName())));
							if (attribute!=null && attributeToReturn.equals(attribute.getNodeName())){
								result = attribute.getNodeValue();
								break;
							}
							
						}
					}
					if (result!=null){
						break;
					}
				}
			}
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return result;
	}
	
}
