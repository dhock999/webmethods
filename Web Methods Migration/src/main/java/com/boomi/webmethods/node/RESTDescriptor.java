package com.boomi.webmethods.node;

import org.w3c.dom.Document;

public class RESTDescriptor extends WMNode {
	
	public static final String TYPE="restDescriptor";

	public RESTDescriptor(Document doc, String filePath) {
		super(doc, filePath);
	}
	
	@Override
	public String getType()
	{
		return TYPE;
	}
}
