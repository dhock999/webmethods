package com.boomi.webmethods;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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

import com.boomi.webmethods.flow.Flow;
import com.boomi.webmethods.node.FlowService;
import com.boomi.webmethods.node.RESTDescriptor;
import com.boomi.webmethods.node.WMNode;
import com.boomi.webmethods.node.WMNodeFactory;
import com.boomi.webmethods.node.WebServiceDescriptor;
import com.boomi.webmethods.util.Util;

public class ProjectAnalyzer {

	private Map<String, WMNode> nodes = new TreeMap<String, WMNode>(); 
	private Map<String, Flow> flows = new TreeMap<String, Flow>(); 
	private List<Flow> flowList;
	private List<WMNode> nodeList;
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
		dictionaryOut.append("<Flows>");
		Iterator<String> flowIter = flows.keySet().iterator();
		Iterator<String> iterator;
		while (flowIter.hasNext())
		{
			Flow flow = flows.get(flowIter.next());
			dictionaryOut.append("<Flow>");
			dictionaryOut.append("<FullName>"+flow.getFullName()+"</FullName>");
			dictionaryOut.append("<Name>"+flow.getName()+"</Name>");
			dictionaryOut.append("<Type>"+flow.getType()+"</Type>");
			dictionaryOut.append("<Package>"+flow.getPackage()+"</Package>");
			dictionaryOut.append("<FilePath>"+flow.getFilePath()+"</FilePath>");
		
			dictionaryOut.append("<InternalDependencies>");
			iterator=flow.getInternalNodeDependencies().iterator();
			while(iterator.hasNext())
			{
				dictionaryOut.append("<Dependency>"+iterator.next()+"</Dependency>");				
			}
			dictionaryOut.append("</InternalDependencies>");
			
			dictionaryOut.append("<ExternalServiceDependencies>");
			iterator=flow.getExternalServiceDependencies().iterator();
			while(iterator.hasNext())
			{
				dictionaryOut.append("<Dependency>"+iterator.next()+"</Dependency>");				
			}
			dictionaryOut.append("</ExternalServiceDependencies>");
			
			dictionaryOut.append("<InternalServiceDependencies>");
			iterator=flow.getInternalServiceDependencies().iterator();
			while(iterator.hasNext())
			{
				dictionaryOut.append("<Dependency>"+iterator.next()+"</Dependency>");				
			}
			dictionaryOut.append("</InternalServiceDependencies>");
			
			dictionaryOut.append("<References>");
			iterator=getWhereFlowUsed(flow).iterator();
			while(iterator.hasNext())
			{
				dictionaryOut.append("<Reference>"+iterator.next()+"</Reference>");				
			}
			dictionaryOut.append("</References>");

			dictionaryOut.append("</Flow>");			
		}
		dictionaryOut.append("</Flows>");
		
		dictionaryOut.append("<Nodes>");
		Iterator<String> nodeIter = nodes.keySet().iterator();
		while (nodeIter.hasNext())
		{
			WMNode node = nodes.get(nodeIter.next());
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
        Source xslt = new StreamSource(Util.getResourceAsStream("dictionary.xsl", this.getClass()));
        Transformer transformer = factory.newTransformer(xslt);

        Source text = new StreamSource(new ByteArrayInputStream(this.getDictionaryXML().getBytes()));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        transformer.transform(text, new StreamResult(baos));
        return baos.toString();        
	}
	
	public String getDependencyReport() throws IOException {
		dependencyOut = new StringBuilder();
		dependencyOut.append("<html>");
		
		dependencyOut.append("<head>\r\n" + 
		"    <meta charset=\"utf-8\">\r\n" + 
		"    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1, shrink-to-fit=no\">\r\n" + 
		"    <title>Basic Bootstrap Template</title>\r\n" + 
		"    <!-- Bootstrap CSS file -->\r\n" + 
		"    <link rel=\"stylesheet\" href=\"https://stackpath.bootstrapcdn.com/bootstrap/4.3.1/css/bootstrap.min.css\" integrity=\"sha384-ggOyR0iXCbMQv3Xipma34MD+dH/1fQ784/j6cY/iJTQUOhcWr7x9JvoRxT2MZw1T\" crossorigin=\"anonymous\">\r\n" + 
		"</head>");
		dependencyOut.append("<body>");
		dependencyOut.append("<h1>Web Methods Portfolio Analysis</h1>");
		dependencyOut.append("<h2>Component Dependency Report</h2>");
		
		Iterator<String> flowIter = flows.keySet().iterator();
		dependencyOut.append("<h3>Service Dependencies</h3><ol>");
	
		while (flowIter.hasNext())
		{
			Flow flow = flows.get(flowIter.next());
			//Show only top level flows;
			if (getWhereFlowUsed(flow).size()==0)
			{				
				dependencyOut.append("<h4><li>"+flow.getName()+"</li></h4>");
				dependencyOut.append("<table>");
				dependencyOut.append("<tr>");
				dependencyOut.append("<td><b>Type</b></td><td>"+flow.getType()+"</td>");
				dependencyOut.append("</tr>");
				dependencyOut.append("<tr>");
				dependencyOut.append("<td><b>Full Name</b></td><td>"+flow.getFullName()+"</td>");
				dependencyOut.append("</tr>");
				dependencyOut.append("<tr>");
				dependencyOut.append("<td><b>Package</b></td><td>"+flow.getPackage()+"</td>");
				dependencyOut.append("</tr>");
				dependencyOut.append("</table>");

				dumpFlowDependencies(flow);	
			}
		}
		
		Iterator<String> nodeIter = nodes.keySet().iterator();
		dependencyOut.append("</ol><h2>API Dependencies</h2><ol>");
		while (nodeIter.hasNext())
		{
			WMNode node = nodes.get(nodeIter.next());
			if ((node.getType().contentEquals(RESTDescriptor.TYPE) && !node.getSubType().contentEquals("consumerDescriptor")) || node.getType().contentEquals(WebServiceDescriptor.TYPE))
			{
				//Show only top level apis;
				if (getWhereNodeUsed(node).size()==0)
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
					dumpNodeDependencies(node.getDependencies());	
				}	
			}
		}
		
		dependencyOut.append("</ol></body></html>");
		return dependencyOut.toString();
	}
	
	private List<String> getWhereFlowUsed(Flow targetFlow)
	{
		List<String> list = new ArrayList<String>();
		Iterator<String> flowIter = flows.keySet().iterator();
		while (flowIter.hasNext())
		{
			Flow flow = flows.get(flowIter.next());
			if (flow.getInternalServiceDependencies().contains(targetFlow.getFullName()))
				list.add(flow.getFullName());
		}

		Iterator<String> nodeIter = nodes.keySet().iterator();
		while (nodeIter.hasNext())
		{
			WMNode node = nodes.get(nodeIter.next());
			if (node.getDependencies().contains(targetFlow.getFullName()))
				list.add(node.getFullName());
		}
		return list;
	}
	
	private List<String> getWhereNodeUsed(WMNode targetNode)
	{
		List<String> list = new ArrayList<String>();
		
		Iterator<String> flowIter = flows.keySet().iterator();
		while (flowIter.hasNext())
		{
			Flow flow = flows.get(flowIter.next());
			if (flow.getInternalServiceDependencies().contains(targetNode.getFullName()))
				list.add(flow.getFullName());
		}		
		
		Iterator<String> nodeIter = nodes.keySet().iterator();
		while (nodeIter.hasNext())
		{
			WMNode node = nodes.get(nodeIter.next());
			if (node.getDependencies().contains(targetNode.getFullName()))
				list.add(node.getFullName());
		}
		return list;
	}

	private void dumpFlowDependencies(Flow flow)
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
					Flow subFlow = flows.get(serviceName);
					if (subFlow!=null)
					{
						dependencyOut.append("<li>"+subFlow.toString()+"</li>");
						dumpFlowDependencies(subFlow);
					}
					WMNode subNode = nodes.get(serviceName);
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
		dumpNodeDependencies(flow.getInternalNodeDependencies());
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
				if (this.isValidComponent(nodeName))
				{
					WMNode node = nodes.get(nodeName);
					
					//TODO due to our kludgy getDependencies scanning of raw Node XML, some fullnames found in api descriptor are not actuall nodes
					if (node!=null) 
					{
						dependencyOut.append("<li>"+node.toString()+"</li>");	
						if (node.getType().contentEquals(FlowService.TYPE) && !node.getSubType().contentEquals("restConsumer"))
						{
							Flow flow = flows.get(node.getFullName());
							dumpFlowDependencies(flow);
						} else {
//							System.out.append("flow:"+node);
							dumpNodeDependencies(node.getDependencies());
						}
					}
				}
			}
			dependencyOut.append("</ul>");	
		}		
	}
	private boolean isValidComponent (String fullName)
	{
		return flows.get(fullName)!=null || nodes.get(fullName)!=null;
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
            	if (entry.getName().endsWith("/node.ndf"))
            	{
            		WMNode node = WMNodeFactory.getNode(entry.getName(), zis);
            		nodes.put(node.getFullName(), node);      		
            	}
            	if (entry.getName().endsWith("/flow.xml"))
            	{
            		Flow flow = new Flow(entry.getName(), zis);
            		flows.put(flow.getFullName(), flow);
            	}
            }
       
            zis.closeEntry();
            entry = zis.getNextEntry();
        }
	}

	public List<Flow> getFlowList() {
		flowList = new ArrayList<Flow>();
		Iterator<String> flowKeys = flows.keySet().iterator();
		while (flowKeys.hasNext())
		{
			flowList.add(flows.get(flowKeys.next()));
		}
		//TODO sort by getName
		return flowList;
	}

	public List<WMNode> getNodeList() {
		nodeList = new ArrayList<WMNode>();
		Iterator<String> nodeKeys = nodes.keySet().iterator();
		while (nodeKeys.hasNext())
		{
			nodeList.add(nodes.get(nodeKeys.next()));
		}
		//TODO Sort by getType, getName
		return nodeList;
	}
}
