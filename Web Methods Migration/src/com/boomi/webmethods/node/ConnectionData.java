package com.boomi.webmethods.node;

import java.util.Map;
import java.util.TreeMap;

import org.w3c.dom.Document;

public class ConnectionData extends WMNode {

	public static final String TYPE="ConnectionData";

	public ConnectionData(Document doc, String filePath) {
		super(doc, filePath);
	}
	
	public Map<String,String> getProperties()
	{
		Map<String,String> properties = new TreeMap<String,String>();
		properties.put("adapterTypeName", super.findEncodedValue("adapterTypeName"));
		properties.put("connectionFactoryTypeName", super.findEncodedValue("connectionFactoryTypeName"));
		properties.put("transactionType", super.findEncodedValue("transactionType"));
		properties.put("datasourceClass", super.findEncodedValue("datasourceClass"));
		properties.put("serverName", super.findEncodedValue("serverName"));
		properties.put("user", super.findEncodedValue("user"));
		properties.put("password", super.findEncodedValue("password"));
		properties.put("databaseName", super.findEncodedValue("databaseName"));
		properties.put("portNumber", super.findEncodedValue("portNumber"));
		return properties;
	}
	
	@Override
	public String getType()
	{
		return TYPE;
	}
}
