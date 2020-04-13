package com.boomi.webmethods;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

import com.boomi.webmethods.flow.Flow;
import com.boomi.webmethods.node.FlowService;
import com.boomi.webmethods.node.RESTDescriptor;
import com.boomi.webmethods.node.WMNode;
import com.boomi.webmethods.node.WMNodeFactory;
import com.boomi.webmethods.node.WebServiceDescriptor;

public class ProjectAnalyzer {

	private Map<String, WMNode> nodes = new TreeMap<String, WMNode>(); 
	private Map<String, Flow> flows = new TreeMap<String, Flow>(); 
	private PrintWriter dependencyOut;
	private PrintWriter dictionaryOut;
	private String integrationServerPath;
	public ProjectAnalyzer (String integrationServerPath) throws XPathExpressionException, IOException, ParserConfigurationException, SAXException
	{
		this.integrationServerPath=integrationServerPath;
		crawlDictionaryFiles(integrationServerPath);
	}

	public void writeDictionary() throws IOException {
		// TODO Auto-generated method stub
		dictionaryOut = new PrintWriter(new BufferedWriter(new FileWriter(integrationServerPath+"\\dictionary.xml")));
		dictionaryOut.println("<Dictionary>");
		dictionaryOut.println("<Flows>");
		Iterator<String> flowIter = flows.keySet().iterator();
		Iterator<String> iterator;
		while (flowIter.hasNext())
		{
			Flow flow = flows.get(flowIter.next());
			dictionaryOut.println("<Flow>");
			dictionaryOut.println("<FullName>"+flow.getFullName()+"</FullName>");
			dictionaryOut.println("<Name>"+flow.getName()+"</Name>");
			dictionaryOut.println("<Package>"+flow.getPackage()+"</Package>");
			dictionaryOut.println("<FilePath>"+flow.getFilePath()+"</FilePath>");
		
			dictionaryOut.println("<InternalDependencies>");
			iterator=flow.getInternalNodeDependencies().iterator();
			while(iterator.hasNext())
			{
				dictionaryOut.println("<Dependency>"+iterator.next()+"</Dependency>");				
			}
			dictionaryOut.println("</InternalDependencies>");
			
			dictionaryOut.println("<ExternalServiceDependencies>");
			iterator=flow.getExternalServiceDependencies().iterator();
			while(iterator.hasNext())
			{
				dictionaryOut.println("<Dependency>"+iterator.next()+"</Dependency>");				
			}
			dictionaryOut.println("</ExternalServiceDependencies>");
			
			dictionaryOut.println("<InternalServiceDependencies>");
			iterator=flow.getInternalServiceDependencies().iterator();
			while(iterator.hasNext())
			{
				dictionaryOut.println("<Dependency>"+iterator.next()+"</Dependency>");				
			}
			dictionaryOut.println("</InternalServiceDependencies>");
			
			dictionaryOut.println("<References>");
			iterator=getWhereFlowUsed(flow).iterator();
			while(iterator.hasNext())
			{
				dictionaryOut.println("<Reference>"+iterator.next()+"</Reference>");				
			}
			dictionaryOut.println("</References>");

			dictionaryOut.println("</Flow>");			
		}
		dictionaryOut.println("</Flows>");
		
		dictionaryOut.println("<Nodes>");
		Iterator<String> nodeIter = nodes.keySet().iterator();
		while (nodeIter.hasNext())
		{
			WMNode node = nodes.get(nodeIter.next());
			dictionaryOut.println("<Node>");
			dictionaryOut.println("<Name>"+node.getName()+"</Name>");
			dictionaryOut.println("<Type>"+node.getType()+"</Type>");
			dictionaryOut.println("<FullName>"+node.getFullName()+"</FullName>");
			dictionaryOut.println("<Package>"+node.getPackage()+"</Package>");
			dictionaryOut.println("<FilePath>"+node.getFilePath()+"</FilePath>");
			
			dictionaryOut.println("<Properties>");
			
			Map<String,String> properties = node.getProperties();
			Iterator<String> keyIterator=properties.keySet().iterator();
			while(keyIterator.hasNext())
			{
				String key = keyIterator.next();
				dictionaryOut.println("<Property>");
				dictionaryOut.println("<Name>" + key + "</Name>");
				dictionaryOut.println("<Value>" + properties.get(key) + "</Value>");
				dictionaryOut.println("</Property>");				
			}
			dictionaryOut.println("</Properties>");
			
			dictionaryOut.println("<Dependencies>");
			iterator=node.getDependencies().iterator();
			while(iterator.hasNext())
			{
				dictionaryOut.println("<Dependency>"+iterator.next()+"</Dependency>");				
			}
			dictionaryOut.println("</Dependencies>");
			
			dictionaryOut.println("<References>");
			iterator=getWhereNodeUsed(node).iterator();
			while(iterator.hasNext())
			{
				dictionaryOut.println("<Reference>"+iterator.next()+"</Reference>");				
			}
			dictionaryOut.println("</References>");
			
			dictionaryOut.println("</Node>");			
		}
		dictionaryOut.println("</Nodes>");
		dictionaryOut.println("</Dictionary>");
		dictionaryOut.close();		
	}

	public void writeDependencyReport() throws IOException {
		dependencyOut = new PrintWriter(new BufferedWriter(new FileWriter(integrationServerPath+"\\dependency.html")));
		dependencyOut.println("<html>");
		dependencyOut.println("<h1>Dependency Report</h1>");
		
		Iterator<String> flowIter = flows.keySet().iterator();
		dependencyOut.println("<h2>Service Dependencies</h2>");
		while (flowIter.hasNext())
		{
			Flow flow = flows.get(flowIter.next());
			//Show only top level flows;
			if (getWhereFlowUsed(flow).size()==0)
			{				
				dependencyOut.println("<h2>"+flow.getName()+"</h2>");
				dependencyOut.println("<b>Type: </b>"+flow.getType()+"<br/>");
				dependencyOut.println("<b>Full Name:</b> "+flow.getFullName()+"<br/>");
				dependencyOut.println("<b>Package: </b>"+flow.getPackage()+"<br/>");
				dumpFlowDependencies(flow);	
			}
		}
		
		Iterator<String> nodeIter = nodes.keySet().iterator();
		dependencyOut.println("<h2>API Dependencies</h2>");
		while (nodeIter.hasNext())
		{
			WMNode node = nodes.get(nodeIter.next());
			if (node.getType().contentEquals(RESTDescriptor.TYPE) || node.getType().contentEquals(WebServiceDescriptor.TYPE))
			{
				//Show only top level apis;
				if (getWhereNodeUsed(node).size()==0)
				{					
					dependencyOut.println("<h2>"+node.getName()+"</h2>");
					dependencyOut.println("<b>Type: </b>"+node.getType()+"<br/>");
					dependencyOut.println("<b>Full Name:</b> "+node.getFullName()+"<br/>");
					dependencyOut.println("<b>Package: </b>"+node.getPackage()+"<br/>");
					dumpNodeDependencies(node.getDependencies());	
				}	
			}
		}
		
		dependencyOut.println("</html>");
		dependencyOut.close();
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
			dependencyOut.println("<ul><b>Internal Service Dependencies</b><br/>");
			while (subFlowIter.hasNext())
			{
				String serviceName=subFlowIter.next();
				if (!serviceName.contentEquals(flow.getFullName()))
				{
					Flow subFlow = flows.get(serviceName);
					if (subFlow!=null)
					{
						dependencyOut.println("<li>"+subFlow.toString()+"</li>");
						dumpFlowDependencies(subFlow);
					}
					WMNode subNode = nodes.get(serviceName);
					if (subNode!=null)
					{
						dependencyOut.println("<li>"+subNode.toString()+"</li>");
						dumpNodeDependencies(subNode.getDependencies());
					}
				}					
			}
			dependencyOut.println("</ul>");
		}
		
		List<String> externalDeps = flow.getExternalServiceDependencies();
		if (externalDeps.size()>0)
		{
			dependencyOut.println("<ul><b>External Service Dependencies</b><br/>");
			for (int i=0; i<externalDeps.size(); i++)
			{
				dependencyOut.println("<li>"+externalDeps.get(i)+"</li>");
			}
			dependencyOut.println("</ul>");
		}
//		System.out.println("267"+flow);
		dumpNodeDependencies(flow.getInternalNodeDependencies());
	}
	
	private void dumpNodeDependencies(List<String> subNodes)
	{
		Iterator<String> nodeIter = subNodes.iterator();
		if(nodeIter.hasNext())
		{
			dependencyOut.println("<ul><b>Node Dependencies</b><br/>");
			while (nodeIter.hasNext())
			{
				String nodeName = nodeIter.next();
				if (this.isValidComponent(nodeName))
				{
					WMNode node = nodes.get(nodeName);
					
					//TODO due to our kludgy getDependencies scanning of raw Node XML, some fullnames found in api descriptor are not actuall nodes
					if (node==null) System.out.println(nodeName);
					else
					{
						dependencyOut.println("<li>"+node.toString()+"</li>");	
						if (node.getType().contentEquals(FlowService.TYPE) && !node.getSubType().contentEquals("restConsumer"))
						{
							Flow flow = flows.get(node.getFullName());
							dumpFlowDependencies(flow);
						} else {
//							System.out.println("flow:"+node);
							dumpNodeDependencies(node.getDependencies());
						}
					}
				}
			}
			dependencyOut.println("</ul>");	
		}		
	}
	private boolean isValidComponent (String fullName)
	{
		return flows.get(fullName)!=null || nodes.get(fullName)!=null;
	}
	
	private void crawlDictionaryFiles( String path ) throws XPathExpressionException, IOException, ParserConfigurationException, SAXException {

        File root = new File( path );
        File[] list = root.listFiles();

        if (list == null) return;

        for ( File f : list ) {
            if ( f.isDirectory() ) {
            	if (!f.getName().startsWith("Wm"))
            		crawlDictionaryFiles( f.getAbsolutePath() );
            }
            else {
            	if (f.getName().contentEquals("node.ndf"))
            	{
            		WMNode node = WMNodeFactory.getNode(f.getAbsolutePath());
            		nodes.put(node.getFullName(), node);      		
            	}
            	if (f.getName().contentEquals("flow.xml"))
            	{
            		Flow flow = new Flow(f.getAbsolutePath());
            		flows.put(flow.getFullName(),flow);
            	}
            }
        }
    }
}
