package com.boomi.webmethods.node;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.w3c.dom.Document;

public class AdapterService extends WMNode {

	public static final String TYPE="AdapterService";

	public AdapterService(Document doc, String filePath) {
		super(doc, filePath);
		// TODO Auto-generated constructor stub
	}
	
	public Map<String,String> getProperties()
	{
		Map<String,String> properties = new TreeMap<String,String>();
		properties.put("serviceTemplateName", super.findEncodedValue("serviceTemplateName"));
		properties.put("connectionName", super.findEncodedValue("connectionName"));
		
		if (findEncodedValue("serviceTemplateName").contentEquals("com.wm.adapter.wmjdbc.services.CustomSQL"))
		{
			properties.put("sql", super.findEncodedValue("sql"));
			properties.put("sqlFieldType", super.findEncodedValue("sqlFieldType"));
		} else {
			properties.put("transactionType", super.findEncodedValue("transactionType"));
			properties.put("datasourceClass", super.findEncodedValue("datasourceClass"));
			properties.put("serverName", super.findEncodedValue("user"));
			properties.put("user", super.findEncodedValue("sqlFieldType"));
			properties.put("password", super.findEncodedValue("password"));
			properties.put("databaseName", super.findEncodedValue("databaseName"));
			properties.put("portNumber", super.findEncodedValue("portNumber"));
		}
		return properties;
	}
	
	@Override
	public List<String> getDependencies()
	{
		List<String> list = super.getDependencies();
		String connection = this.findEncodedValue("connectionName");
		if (connection.length()>0)
			list.add(connection);
		return list;
	}
	
	@Override
	public String getType()
	{
		return TYPE;
	}
	
	@Override
	public String getBoomiArtifact()
	{
		return "";
	}
}
