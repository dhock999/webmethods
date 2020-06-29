package com.boomi.webmethods.node;

import org.w3c.dom.Document;

public class WebServiceDescriptor extends WMNode {

	public static final String TYPE="webServiceDescriptor";

	public WebServiceDescriptor(Document doc, String filePath) {
		super(doc, filePath);
	}
	
	@Override
	public String getType()
	{
		return TYPE;
	}
}
