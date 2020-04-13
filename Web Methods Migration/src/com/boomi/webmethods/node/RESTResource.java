package com.boomi.webmethods.node;

import java.util.List;

import org.w3c.dom.Document;

public class RESTResource extends WMNode {

	public static final String TYPE="restResource";

	public RESTResource(Document doc, String filePath) {
		super(doc, filePath);
	}
	
	@Override
	public String getType()
	{
		return TYPE;
	}
}
