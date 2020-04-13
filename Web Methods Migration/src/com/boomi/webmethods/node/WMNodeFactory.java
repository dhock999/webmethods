package com.boomi.webmethods.node;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class WMNodeFactory {
	public static WMNode getNode(String filePath) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException  {
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
//        factory.setNamespaceAware(false); // never forget this!
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new File(filePath));
        
		XPath xpath = XPathFactory.newInstance().newXPath();
		
        XPathExpression expr;
        NodeList nodes;

        expr = xpath.compile("/Values/value[@name = 'svc_type']/text()");
        nodes = (NodeList)expr.evaluate(doc, XPathConstants.NODESET);
        
		String type = "";
        if (nodes.getLength()>0)
        	type=nodes.item(0).getNodeValue();
        else {
            expr = xpath.compile("/Values/value[@name = 'node_type']/text()");
            nodes = (NodeList)expr.evaluate(doc, XPathConstants.NODESET);
            if (nodes.getLength()>0)
            	type=nodes.item(0).getNodeValue();
            else {
                expr = xpath.compile("/Values/record/value[@name = 'node_type']/text()");
                nodes = (NodeList)expr.evaluate(doc, XPathConstants.NODESET);
                if (nodes.getLength()>0)
                	type=nodes.item(0).getNodeValue();            	
            }
        }
        if (type!=null)
		switch (type)
		{
		case RESTDescriptor.TYPE:
			return new RESTDescriptor(doc, filePath);
		case RESTResource.TYPE:
			return new RESTResource(doc, filePath);
		case Record.TYPE:
			return new Record(doc, filePath);
		case WebServiceDescriptor.TYPE:
			return new WebServiceDescriptor(doc, filePath);
		case FlatFileSchema.TYPE:
			return new FlatFileSchema(doc, filePath);
		case DocumentPartHolder.TYPE:
			return new DocumentPartHolder(doc, filePath);
		case ConnectionData.TYPE:
			return new ConnectionData(doc, filePath);
		case AdapterService.TYPE:
			return new AdapterService(doc, filePath);
		case FlowService.TYPE:
			return new FlowService(doc, filePath);
		case JavaService.TYPE:
			return new JavaService(doc, filePath);
		}				
     
		System.out.println("Unknown Node Type" + filePath);
		return null;
	}
}
