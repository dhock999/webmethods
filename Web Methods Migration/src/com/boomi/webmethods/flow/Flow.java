package com.boomi.webmethods.flow;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
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

public class Flow {
	private String nodeXML;
	private Document doc;
	private String pkg;
	private String nsName;
	private String filePath;
	private List<String> externalServices;
	private List<String> internalServices;
	private List<String> nodeDependencies = new ArrayList<String>();
	XPath xpath;
	public Flow(String filePath) throws XPathExpressionException, SAXException, IOException, ParserConfigurationException
	{
		this.filePath=filePath;
		nodeXML = readFile(filePath);
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        doc = builder.parse(new File(filePath));
        
		xpath = XPathFactory.newInstance().newXPath();
		
    	//TODO we have to derive from file path for services...kludge but can't find it elsewhere
    	char pathDelimiter = '/';//TODO only for windows file system, not linux
    	if (filePath.indexOf(pathDelimiter)==-1)
    		pathDelimiter='\\';
    	int pos = filePath.lastIndexOf(pathDelimiter+"ns"+pathDelimiter); 
    	if (pos>=0)
    	{
    		pos+=4;//length of /ns/
    		nsName=filePath.substring(pos);
    		nsName=nsName.substring(0, nsName.indexOf(pathDelimiter+"flow.xml"));
    		nsName=nsName.replace(pathDelimiter, '.');
    		pos=nsName.lastIndexOf(".");
    		nsName=nsName.substring(0,pos)+":"+nsName.substring(pos+1);
    	}

    	pos = filePath.lastIndexOf(pathDelimiter+"ns"+pathDelimiter); 
    	if (pos>=0)
    	{
    		pos+=4;//length of /ns/
    		pkg=filePath.substring(pos);
    		pkg=pkg.substring(0, pkg.indexOf(pathDelimiter));
    	}
    	loadDependencies();
	}
	
	private void loadDependencies()
	{
		Set<String> internal = new TreeSet<String>();
		Set<String> external = new TreeSet<String>();
		XPathExpression expr;

		//This is a bitch....we can only include a service if the enclosed maps have inputs....or if their are no maps.
		try {
			expr = xpath.compile("//INVOKE/@SERVICE");
			NodeList serviceNodes = (NodeList)expr.evaluate(doc, XPathConstants.NODESET);
			
			for (int i=0; i<serviceNodes.getLength(); i++)
			{
				String service=serviceNodes.item(i).getNodeValue();
				if (service.contentEquals("receiveAndInsertVehicleTelem"))
					break;
				if (service.startsWith(this.getPackage()+"."))
					internal.add(service);
				else
					external.add(service);
			}
			
			expr = xpath.compile("//MAPINVOKE/@SERVICE");
			serviceNodes = (NodeList)expr.evaluate(doc, XPathConstants.NODESET);
			
			for (int i=0; i<serviceNodes.getLength(); i++)
			{
				String service=serviceNodes.item(i).getNodeValue();
				if (service.contentEquals("receiveAndInsertVehicleTelem"))
					break;
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
		this.nodeDependencies=getDependenciesFromNodeXML(this.getFullName(), nodeXML, this.getPackage(), internalServices);
	}
	
	public static String readFile(String filePath) 
	{
	    StringBuilder contentBuilder = new StringBuilder();
	    try (Stream<String> stream = Files.lines( Paths.get(filePath), StandardCharsets.ISO_8859_1)) 
	    {
	        stream.forEach(s -> contentBuilder.append(s).append("\n"));
	    }
	    catch (IOException e) 
	    {
	        e.printStackTrace();
	    }
	    return contentBuilder.toString();
	}
	
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("Type: Flow Service");
		sb.append(",");
		sb.append("Name: " + getName());
		sb.append(",");
		sb.append("Full Name: " + getFullName());
		sb.append(",");
		sb.append("Package: " + getPackage());
//		sb.append(",");
//		sb.append("File Path: " + filePath);
		return sb.toString();
	}
	
	public String getType()
	{
		return "Flow Service";
	}
	
	public List<String> getInternalNodeDependencies()
	{
		return nodeDependencies;
	}
	
	//TODO this is an ugly way to get dependencies
	public static List<String> getDependenciesFromNodeXML(String fullName, String nodeXML, String pkg, List<String> excludes)
	{
		Set<String> deps = new TreeSet<String>();
		String target = pkg+".";
		int pos=nodeXML.indexOf(target);
		while (pos>0)
		{
			int quotePos = nodeXML.indexOf('"', pos);
			int slashPos = nodeXML.indexOf('/', pos);
			int ltPos = nodeXML.indexOf('<', pos);
			int qPos = nodeXML.indexOf('?', pos);
			int end=quotePos;
			if (slashPos<end && slashPos>pos)
				end=slashPos;
			if (ltPos<end && ltPos>pos)
				end=ltPos;
			if (qPos<end && qPos>pos)
				end=qPos;
			if (end>pos)
			{
				String dep=nodeXML.substring(pos,end);
				//only add non-services
				if (excludes==null || !excludes.contains(dep))
					if (!dep.contentEquals(fullName)) //Don't reference yourself
						deps.add(dep);
				pos=nodeXML.indexOf(target, end);
			}
			else pos=-1;
		}
		List<String> list = new ArrayList<String>();
		Iterator<String> itr = deps.iterator();
		while (itr.hasNext())
			list.add(itr.next());
		return list;
	}

	public String getPackage() {
		return pkg;
	}

	public String getFullName() {
		return nsName;
	}
	
	public String getFilePath() {
		return filePath;
	}
	
	public String getName()
	{
		return getFullName().substring(getFullName().lastIndexOf(":")+1);
	}

	public List<String> getExternalServiceDependencies() {
		return externalServices;
	}

	public List<String> getInternalServiceDependencies() {
		return internalServices;
	}

}
