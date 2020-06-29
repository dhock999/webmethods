package com.boomi.webmethods.node;

import org.w3c.dom.Document;

public class JavaService extends WMNode {

	public static final String TYPE="java";

	public JavaService(Document doc, String filePath) {
		super(doc, filePath);
	}
	
	@Override
	public String getType()
	{
		return TYPE;
	}
}
