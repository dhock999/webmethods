package com.boomi.webmethods.node;

import org.w3c.dom.Document;

public class Record extends WMNode {

	public static final String TYPE="record";

	public Record(Document doc, String filePath) {
		super(doc, filePath);
	}
	
	@Override
	public String getType()
	{
		return TYPE;
	}
}
