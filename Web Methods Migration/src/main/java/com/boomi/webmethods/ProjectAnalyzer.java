package com.boomi.webmethods;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

import com.boomi.webmethods.node.Flow;
import com.boomi.webmethods.node.RESTDescriptor;
import com.boomi.webmethods.node.WMNode;
import com.boomi.webmethods.node.WMNodeFactory;
import com.boomi.webmethods.node.WebServiceDescriptor;
import com.boomi.webmethods.util.Util;

public class ProjectAnalyzer {

	private List<WMNode> nodeList = new ArrayList<WMNode>();
	private StringBuilder dependencyOut;
	private StringBuilder dictionaryOut;
	public ProjectAnalyzer (InputStream is) throws Exception
	{
		this.startCrawlFiles(is);
	}

	public String getDictionaryXML() throws Exception {
		// TODO Auto-generated method stub
		dictionaryOut = new StringBuilder();
		dictionaryOut.append("<Dictionary>");
		
		Map<String, Integer> totals = getTotals();
		dictionaryOut.append("<Totals>");
		Set<String> keys = totals.keySet();
		for (String key: keys)
		{
			dictionaryOut.append("<Total>");			
			dictionaryOut.append("<Type>" + key + "</Type>");
			dictionaryOut.append("<Count>" + totals.get(key) + "</Count>");
			dictionaryOut.append("</Total>");
		}
		dictionaryOut.append("</Totals>");

		
		dictionaryOut.append("<Nodes>");
		Iterator<String> iterator;

		for (WMNode node:getNodeList())
		{
			dictionaryOut.append("<Node>");
			dictionaryOut.append("<Name>"+node.getName()+"</Name>");
			dictionaryOut.append("<Type>"+node.getType()+"</Type>");
			dictionaryOut.append("<SubType>"+node.getSubType()+"</SubType>");
			dictionaryOut.append("<FullName>"+node.getFullName()+"</FullName>");
			dictionaryOut.append("<Package>"+node.getPackage()+"</Package>");
			dictionaryOut.append("<FilePath>"+node.getFilePath()+"</FilePath>");
			
			dictionaryOut.append("<Properties>");
			
			Map<String,String> properties = node.getProperties();
			Iterator<String> keyIterator=properties.keySet().iterator();
			while(keyIterator.hasNext())
			{
				String key = keyIterator.next();
				dictionaryOut.append("<Property>");
				dictionaryOut.append("<Name>" + key + "</Name>");
				dictionaryOut.append("<Value>" + properties.get(key) + "</Value>");
				dictionaryOut.append("</Property>");				
			}
			dictionaryOut.append("</Properties>");
			
			dictionaryOut.append("<Dependencies>");
			iterator=node.getDependencies().iterator();
			while(iterator.hasNext())
			{
				dictionaryOut.append("<Dependency>"+iterator.next()+"</Dependency>");				
			}
			dictionaryOut.append("</Dependencies>");
			
			dictionaryOut.append("<InternalServiceDependencies>");
			iterator=node.getInternalServiceDependencies().iterator();
			while(iterator.hasNext())
			{
				dictionaryOut.append("<Dependency>"+iterator.next()+"</Dependency>");				
			}
			dictionaryOut.append("</InternalServiceDependencies>");

			dictionaryOut.append("<ExternalServiceDependencies>");
			iterator=node.getExternalServiceDependencies().iterator();
			while(iterator.hasNext())
			{
				dictionaryOut.append("<Dependency>"+iterator.next()+"</Dependency>");				
			}
			dictionaryOut.append("</ExternalServiceDependencies>");

			dictionaryOut.append("<References>");
			iterator=getWhereNodeUsed(node).iterator();
			while(iterator.hasNext())
			{
				dictionaryOut.append("<Reference>"+iterator.next()+"</Reference>");				
			}
			dictionaryOut.append("</References>");
			
			dictionaryOut.append("</Node>");			
		}
		dictionaryOut.append("</Nodes>");
		dictionaryOut.append("</Dictionary>");
		return dictionaryOut.toString();
	}

	public String getDictionaryHTML() throws Exception
	{
		
        TransformerFactory factory = TransformerFactory.newInstance();
        Source xslt = new StreamSource(Util.getResourceAsStream("resources/dictionary.xsl", this.getClass()));
        Transformer transformer = factory.newTransformer(xslt);

        Source text = new StreamSource(new ByteArrayInputStream(this.getDictionaryXML().getBytes()));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        transformer.transform(text, new StreamResult(baos));
        return baos.toString();        
	}
	
	public String getDependencyReport() throws IOException {
		dependencyOut = new StringBuilder();
		dependencyOut.append("<div>");
		dependencyOut.append("<h1>Web Methods Portfolio Analysis</h1>");
		dependencyOut.append("<h2>Component Dependency Report</h2>");
		
		dependencyOut.append("<h3>Service Dependencies</h3><ol>");
	
		for (WMNode node:getNodeList())
		{
			if ((node.getType().contentEquals(Flow.TYPE)))
			{
				//Show only top level apis;
				if (getWhereNodeUsed(node).size()==0)
				{					
					dumpDependency(node);
					dumpFlowDependencies(node);	
				}
			}
		}
		dependencyOut.append("</ol><h2>API Dependencies</h2><ol>");
		for (WMNode node:getNodeList())
		{
			if ((node.getType().contentEquals(RESTDescriptor.TYPE) && !node.getSubType().contentEquals("consumerDescriptor")) || node.getType().contentEquals(WebServiceDescriptor.TYPE))
			{
				//Show only top level apis;
				if (getWhereNodeUsed(node).size()==0)
				{					
					dumpDependency(node);
					dumpNodeDependencies(node.getDependencies());
				}
			}
		}
		
		dependencyOut.append("</ol></div>");
		return dependencyOut.toString();
	}
	
	private void dumpDependency(WMNode node)
	{
		dependencyOut.append("<h4><li>"+node.getName()+"</li></h4>");
		dependencyOut.append("<table>");
		dependencyOut.append("<tr>");
		dependencyOut.append("<td><b>Type</b></td><td>"+node.getType()+"</td>");
		dependencyOut.append("</tr>");
		dependencyOut.append("<tr>");
		dependencyOut.append("<td><b>Full Name</b></td><td>"+node.getFullName()+"</td>");
		dependencyOut.append("</tr>");
		dependencyOut.append("<tr>");
		dependencyOut.append("<td><b>Package</b></td><td>"+node.getPackage()+"</td>");
		dependencyOut.append("</tr>");
		dependencyOut.append("</table>");
	}
	
	private List<String> getWhereNodeUsed(WMNode targetNode)
	{
		List<String> list = new ArrayList<String>();
		for (WMNode node:getNodeList())
		{
			if (node.getDependencies().contains(targetNode.getFullName()))
				list.add(node.getFullName());
			if (node.getInternalServiceDependencies().contains(targetNode.getFullName()))
				list.add(node.getFullName());
		}
		return list;
	}

	private void dumpFlowDependencies(WMNode flow)
	{		

		List<String> subFlows=flow.getInternalServiceDependencies();
		Iterator<String> subFlowIter = subFlows.iterator();
		
		if (subFlowIter.hasNext())
		{
			dependencyOut.append("<b>Internal Service Dependencies</b><ul>");
			while (subFlowIter.hasNext())
			{
				String serviceName=subFlowIter.next();
				if (!serviceName.contentEquals(flow.getFullName()))
				{
					WMNode subNode = findNode(serviceName, true);
					if (subNode!=null)
					{
						dependencyOut.append("<li>"+subNode.toString()+"</li>");
						dumpFlowDependencies(subNode);
					}
					subNode = findNode(serviceName, false);
					if (subNode!=null)
					{
						dependencyOut.append("<li>"+subNode.toString()+"</li>");
						dumpNodeDependencies(subNode.getDependencies());
					}
				}					
			}
			dependencyOut.append("</ul>");
		}
		
		List<String> externalDeps = flow.getExternalServiceDependencies();
		if (externalDeps.size()>0)
		{
			dependencyOut.append("<b>External Service Dependencies</b><ul>");
			for (int i=0; i<externalDeps.size(); i++)
			{
				dependencyOut.append("<li>"+externalDeps.get(i)+"</li>");
			}
			dependencyOut.append("</ul>");
		}
//		System.out.append("267"+flow);
		dumpNodeDependencies(flow.getDependencies());
	}
	
	private void dumpNodeDependencies(List<String> subNodes)
	{
		Iterator<String> nodeIter = subNodes.iterator();
		if(nodeIter.hasNext())
		{
			dependencyOut.append("<b>Node Dependencies</b><ul>");
			while (nodeIter.hasNext())
			{
				String nodeName = nodeIter.next();
//				if (this.isValidComponent(nodeName))
				{
					WMNode node = findNode(nodeName, false);
					
					//TODO due to our kludgy getDependencies scanning of raw Node XML, some fullnames found in api descriptor are not actuall nodes
					if (node!=null) 
					{
						dependencyOut.append("<li>"+node.toString()+"</li>");	
						dumpNodeDependencies(node.getDependencies());
					}
				}
			}
			dependencyOut.append("</ul>");	
		}		
	}
	private boolean isValidComponent (String fullName)
	{
		return findNode(fullName, false)!=null;
	}
	
	WMNode findNode(String fullName, Boolean returnFlowService)
	{
		for (WMNode node: nodeList)
		{
			if (node.getFullName().contentEquals(fullName))
			{
				if (returnFlowService && node.getType()==Flow.TYPE)
					return node;
				if (!returnFlowService && node.getType()!=Flow.TYPE)
					return node;
			}
		}
		return null;
	}
	
	private void startCrawlFiles(InputStream is) throws Exception
	{
        ZipInputStream zis = new ZipInputStream(is);
        crawlFiles(zis, zis.getNextEntry());		
        zis.close();
	}
	
	private void crawlFiles(ZipInputStream zis, ZipEntry entry) throws IOException, XPathExpressionException, ParserConfigurationException, SAXException
	{
        while(entry != null) {

            if (!entry.isDirectory()) {      
            	if (entry.getName().endsWith("/node.ndf") || entry.getName().endsWith("/flow.xml"))
            	{
            		WMNode node = WMNodeFactory.getNode(entry.getName(), zis);
            		if (node!=null)
            			nodeList.add(node);
            	}
            }
       
            zis.closeEntry();
            entry = zis.getNextEntry();
        }
	}

	public List<WMNode> getNodeList() {

		nodeList.sort(new Comparator<WMNode>() {
		    @Override
		    public int compare(WMNode n1, WMNode n2) {
		    	if (n1.getType()==n2.getType())
		    		return n1.getName().compareToIgnoreCase(n2.getName());
		    	return n1.getType().toString().compareToIgnoreCase(n2.getType().toString());
		     }
		});
		return nodeList;
	}
	
	private Map<String, Integer> getTotals()
	{
		Map<String, Integer> totals = new TreeMap<String, Integer>();
		for (WMNode node: nodeList)
		{
			int total = 0;
			if (totals.get(node.getType())!=null)
				total = totals.get(node.getType());
			totals.put(node.getType(), total+1);
		}
		return totals;	
	}
}
