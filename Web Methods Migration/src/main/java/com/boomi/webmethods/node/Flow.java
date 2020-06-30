package com.boomi.webmethods.node;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.boomi.webmethods.node.WMNode;
import com.boomi.webmethods.util.Util;

public class Flow extends WMNode
{
	private List<String> externalServices;
	private List<String> internalServices;
	private List<String> nodeDependencies = new ArrayList<String>();
	XPath xpath;
	public static final String TYPE="Flow Service";
	
	public Flow(Document doc, String filePath) {
		super(doc, filePath);
		xpath = XPathFactory.newInstance().newXPath();
		loadDependencies();
	}
	
	@Override
	public String getType()
	{
		return TYPE;
	}
	
//	public Flow(String filePath, ZipInputStream zis) throws XPathExpressionException, SAXException, IOException, ParserConfigurationException
//	{
//		this.filePath=filePath;
//		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
//        DocumentBuilder builder = factory.newDocumentBuilder();
//        nodeXML=Util.zipInputStreamToString(zis);
//        doc = builder.parse(new ByteArrayInputStream(nodeXML.getBytes()));
//        
//		xpath = XPathFactory.newInstance().newXPath();
//		
//    	//TODO we have to derive from file path for services...kludge but can't find it elsewhere
//    	char pathDelimiter = '/';//TODO only for windows file system, not linux
//    	if (filePath.indexOf(pathDelimiter)==-1)
//    		pathDelimiter='\\';
//    	int pos = filePath.lastIndexOf(pathDelimiter+"ns"+pathDelimiter); 
//    	if (pos>=0)
//    		pos+=4;//length of /ns/
//    	else if (filePath.startsWith("ns"+pathDelimiter))
//    		pos=3;    	
//    	if (pos>=0)
//    	{
//    		nsName=filePath.substring(pos);
//    		nsName=nsName.substring(0, nsName.indexOf(pathDelimiter+"flow.xml"));
//    		nsName=nsName.replace(pathDelimiter, '.');
//    		pos=nsName.lastIndexOf(".");
//    		nsName=nsName.substring(0,pos)+":"+nsName.substring(pos+1);
//    	}
//
//    	pos = filePath.lastIndexOf(pathDelimiter+"ns"+pathDelimiter); 
//    	if (pos>=0)
//    		pos+=4;//length of /ns/
//    	else if (filePath.startsWith("ns"+pathDelimiter))
//    		pos=3;    	
//    	if (pos>=0)
//    	{
//    		pkg=filePath.substring(pos);
//    		pkg=pkg.substring(0, pkg.indexOf(pathDelimiter));
//    	}
//    	loadDependencies();
//	}
	
	private void loadDependencies()
	{
		Set<String> internal = new TreeSet<String>();
		Set<String> external = new TreeSet<String>();
		XPathExpression expr;

		//This is a bitch....we can only include a service if the enclosed maps have inputs....or if their are no maps.
		try {
			expr = xpath.compile("//INVOKE/@SERVICE");
			NodeList serviceNodes = (NodeList)expr.evaluate(this.getDocument(), XPathConstants.NODESET);
			
			for (int i=0; i<serviceNodes.getLength(); i++)
			{
				String service=serviceNodes.item(i).getNodeValue();
				if (service.startsWith(this.getPackage()+"."))
					internal.add(service);
				else
					external.add(service);
			}
			
			expr = xpath.compile("//MAPINVOKE/@SERVICE");
			serviceNodes = (NodeList)expr.evaluate(this.getDocument(), XPathConstants.NODESET);
			
			for (int i=0; i<serviceNodes.getLength(); i++)
			{
				String service=serviceNodes.item(i).getNodeValue();
				if (service.startsWith(this.getPackage()+"."))
					internal.add(service);
				else
					external.add(service);
			}
			
//			expr = xpath.compile("//INVOKE");
//			NodeList invokeNodes = (NodeList)expr.evaluate(doc, XPathConstants.NODESET);
//					
//			for (int i=0; i<invokeNodes.getLength(); i++)
//			{
//				boolean includeService=true;
//				Element invokeElem = (Element)invokeNodes.item(i);
//				NodeList mapNodes=invokeElem.getElementsByTagName("MAP");
//				for (int j=0; j<serviceAttributes.getLength(); j++)
//				{
//					
//				}
//				if (includeService)
//				{
//					NodeList serviceAttributes=invokeElem.getElementsByTagName("SERVICE");
//					String service=serviceNodes.item(0).getNodeValue();
//					if (service.contentEquals("receiveAndInsertVehicleTelem"))
//						break;
//					if (service.startsWith(this.getPackage()+"."))
//						internal.add(service);
//					else
//						external.add(service);
//				}
//			}
//
//			expr = xpath.compile("//INVOKEMAP/@SERVICE");
//			serviceNodes = (NodeList)expr.evaluate(doc, XPathConstants.NODESET);
//			for (int i=0; i<serviceNodes.getLength(); i++)
//			{
//				expr = xpath.compile("//MAPINVOKE/MAP/child::node()");
//				NodeList mapChildren = (NodeList)expr.evaluate(doc, XPathConstants.NODESET);
//				for (int x=0; x<mapChildren.getLength(); x++)
//				{
//					//exclude if both INVOKEINPUT and INVOKEOUTPUT are null....just residue of a delete
//					Node mapChild = mapChildren.item(i);
//					if (mapChild.getNodeType()==Node.ELEMENT_NODE)
//					{
//						String service=serviceNodes.item(i).getNodeValue();
//						if (service.contentEquals("receiveAndInsertVehicleTelem"))
//							break;
//						if (service.startsWith(this.getPackage()+"."))
//							internal.add(service);
//						else
//							external.add(service);
//					}
//				}
//			}
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		internalServices = new ArrayList<String>();
		for(String value:internal)
		{
			internalServices.add(value);
		}
		externalServices = new ArrayList<String>();
		for(String value:external)
		{
			externalServices.add(value);
		}
		
		//We need to filter out Services that are found
		this.nodeDependencies=getDependenciesFromNodeXML(this.getFullName(), this.getNodeXML(), this.getPackage(), internalServices);
	}
	
	@Override
	public List<String> getExternalServiceDependencies() {
		return externalServices;
	}

	@Override
	public List<String> getInternalServiceDependencies() {
		return internalServices;
	}

	@Override
	public List<String> getDependencies() {
		return internalServices;
		//return this.nodeDependencies;
	}

}
