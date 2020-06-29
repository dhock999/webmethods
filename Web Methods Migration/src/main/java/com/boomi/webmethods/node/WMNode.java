package com.boomi.webmethods.node;


import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.boomi.webmethods.flow.Flow;
import com.boomi.webmethods.util.Util;

public abstract class WMNode {
	private String pkg;
	private String nsName;
	private String base64Properties;
	private Document doc;
	private String filePath;
	private String nodeXML;
	private String subType="";
	public WMNode(Document doc, String filePath) 
	{
		this.filePath=filePath;
		nodeXML = Util.docToString(doc);
        try {	
        	this.doc = doc;
        	this.filePath = filePath;
    		XPath xpath = XPathFactory.newInstance().newXPath();
    		NodeList nodes;
    		
            XPathExpression expr;
           
            //Subtype
            expr = xpath.compile("/Values/value[@name = 'svc_subtype' or @name = 'node_subtype']/text()");
            nodes = (NodeList)expr.evaluate(doc, XPathConstants.NODESET);
            if (nodes.getLength()>0)
            	subType=nodes.item(0).getNodeValue();
            
            //Full Name
            expr = xpath.compile("/Values/value[@name = 'node_nsName']/text()");
            nodes = (NodeList)expr.evaluate(doc, XPathConstants.NODESET);
            if (nodes.getLength()>0)
            	nsName=nodes.item(0).getNodeValue();
            else {
                expr = xpath.compile("/Values/records/value[@name = 'node_nsName']/text()");
                nodes = (NodeList)expr.evaluate(doc, XPathConstants.NODESET);
                if (nodes.getLength()>0)
                	nsName=nodes.item(0).getNodeValue();            	
                else {
                	//TODO we have to derive from file path for services...kludge but can't find it elsewhere
                	char pathDelimiter = '/';//TODO only for windows file system, not linux
                	if (filePath.indexOf(pathDelimiter)==-1)
                		pathDelimiter='\\';
                	int pos = filePath.lastIndexOf(pathDelimiter+"ns"+pathDelimiter); 
                	if (pos>=0)
                	{
                		pos+=4;//length of /ns/
                		nsName=filePath.substring(pos);
                		nsName=nsName.substring(0, nsName.indexOf(pathDelimiter+"node.ndf"));
                		nsName=nsName.replace(pathDelimiter, '.');
                		pos=nsName.lastIndexOf(".");
                		nsName=nsName.substring(0,pos)+":"+nsName.substring(pos+1);
                	}
                }
            }
            
            //Package
            expr = xpath.compile("/Values/value[@name = 'node_pkg']/text()");
            nodes = (NodeList)expr.evaluate(doc, XPathConstants.NODESET);
            if (nodes.getLength()>0)
            	pkg=nodes.item(0).getNodeValue();
            else {
                expr = xpath.compile("/Values/records/value[@name = 'node_pkg']/text()");
                nodes = (NodeList)expr.evaluate(doc, XPathConstants.NODESET);
                if (nodes.getLength()>0)
                	pkg=nodes.item(0).getNodeValue();            	
                else {
                	//TODO we have to derive from file path for services...kludge but can't find it elsewhere
                	char pathDelimiter = '/';//TODO only for windows file system, not linux
                	if (filePath.indexOf(pathDelimiter)==-1)
                		pathDelimiter='\\';
                	int pos = filePath.lastIndexOf(pathDelimiter+"ns"+pathDelimiter); 
                	if (pos>=0)
                	{
                		pos+=4;//length of /ns/
                		pkg=filePath.substring(pos);
                		pkg=pkg.substring(0, pkg.indexOf(pathDelimiter));
                	}
                }
            }

            
            expr = xpath.compile("/Values/value[@name = 'IRTNODE_PROPERTY']/text()");
            nodes = (NodeList)expr.evaluate(doc, XPathConstants.NODESET);           
            
            if (nodes==null || nodes.getLength()==0)
            {
                expr = xpath.compile("/Values/value[@name = 'IDataEncoded']/text()");
                nodes = (NodeList)expr.evaluate(doc, XPathConstants.NODESET);
            }
    			
            if (nodes.getLength()>0)
    		{
    			String base64 = nodes.item(0).getNodeValue();
    			byte bytes[] = Base64.getDecoder().decode(base64.trim().replaceAll("\n", ""));
    			base64Properties = new String(bytes, StandardCharsets.UTF_16);
    		}
        } catch (Exception e)
        {
        	e.printStackTrace();
        }
	}
	
	public abstract String getType();

	public String encodedValueToString(String tag)
	{
		return tag + ": " + findEncodedValue(tag);
	}
	
	public String findEncodedValue(String tag)
	{
		String src = this.getBase64Properties();
		if (src == null)
			return "";
		StringBuilder value = new StringBuilder();
		int start = src.indexOf(tag);
		if (start>-1)
		{
			start+=tag.length()+1;
			start=findNextString(src, start);
			while (start>-1 && start<src.length() && src.charAt(start)>=' ' && src.charAt(start)<='z')
			{
				value.append(src.charAt(start));
				start++;
			}
		}
		
		return value.toString();
	}
	
	private static int findNextString(String src, int start)
	{
		while (start<src.length() && (src.charAt(start)<' ' || src.charAt(start)>'z'))
			start++;
		if (start>=src.length())
			start=-1;
		return start;
	}
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("Type: ");
		sb.append(getType());
		sb.append(",");
		sb.append("Name: ");
		sb.append(getName());
		sb.append(",");
		sb.append("Full Name: ");
		sb.append(getFullName());
		sb.append(",");
		sb.append("Package: ");
		sb.append(getPackage());
//		sb.append(",");
//		sb.append("File Path: ");
//		sb.append(getFilePath());
		return sb.toString();
	}
	
	public String getPackage() {
		return pkg;
	}
	public String getName()
	{
		return getFullName().substring(getFullName().lastIndexOf(":")+1);
	}
	public String getFullName() {
		return nsName;
	}
	public String getFilePath() {
		return filePath;
	}
	public Document getDocument() {
		return doc;
	}
	private String getBase64Properties() {
		return base64Properties;
	}
	public List<String> getDependencies()
	{
		return Flow.getDependenciesFromNodeXML(this.getFullName(), nodeXML, pkg, null);
	}
	public Map<String,String> getProperties()
	{
		return new TreeMap<String,String>();
	}
	public String getSubType()
	{
		return subType;
	}
	public String getBoomiArtifact()
	{
		return "";
	}
}
