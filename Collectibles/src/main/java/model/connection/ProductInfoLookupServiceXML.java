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
	
	protected Document fetchDocFromUrl(String requestUrl) throws TooFastConnectionException,FileNotFoundException {
    	
    	DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = null;
		try {
			db = dbf.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			logger.error("Exception for document parser", e);
			return null;
		}
        Document doc = null;
        try {
        	doc = db.parse(requestUrl);
        } catch (IOException ioe) {
        	logger.error("Exception reaching out to XML through internet", ioe);
    		if (ioe.getMessage()!=null && ioe.getMessage().contains("HTTP response code: 503")){    			
    			throw new TooFastConnectionException();    				
    		}    		
        	return null;
        } catch (SAXException saxe) {
        	logger.error("SAXE exception", saxe);
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
		} catch (Exception e){
			logger.error("Exception obtaining field from XPath", e);
		}
		
		return field;
	}
	
	protected NodeList getNodes(Document doc,String xpath){
		NodeList nodes = null;
		XPath xPath = XPathFactory.newInstance().newXPath();	
		try {
			nodes = (NodeList)xPath.evaluate(xpath, doc.getDocumentElement(),XPathConstants.NODESET);
			
		} catch (XPathExpressionException e) {			
			logger.error("Exception obtaining field from XPath", e);
		} catch (Exception e){
			logger.error("Generic Exception obtaining field from XPath", e);
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
			logger.error("Exception obtaining field from XPath", e);
		} catch (Exception e){
			logger.error("Generic Exception obtaining field from XPath", e);
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
					logger.debug("Node: " +node );
					if (attributes!=null && attributes.getLength()>0) {
						for (int j=0;j<attributes.getLength();j++) {							
							Node attribute = attributes.item(j);	
							logger.debug("Attribute: " +attribute + "[name: " + attribute.getNodeName() + ", value: " + attribute.getNodeValue() + "]" );
							logger.debug(attributeToReturn + "=" + attribute.getNodeName() +"? " + (attribute!=null && attributeToReturn.equals(attribute.getNodeName())));
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
			logger.error("Exception obtaining attribute from XPath", e);
		}
		
		return result;
	}
	
}
