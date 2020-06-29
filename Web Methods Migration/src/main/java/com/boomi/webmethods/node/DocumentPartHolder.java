package com.boomi.webmethods.node;

import org.w3c.dom.Document;

public class DocumentPartHolder extends WMNode {

	public static final String TYPE="Document Part Holder";

	public DocumentPartHolder(Document doc, String filePath) {
		super(doc, filePath);
	}
	
	@Override
	public String getType()
	{
		return TYPE;
	}
}
