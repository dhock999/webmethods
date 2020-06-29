package com.boomi.webmethods.node;

import org.w3c.dom.Document;

public class FlowService extends WMNode {

	public static final String TYPE="flow";

	public FlowService(Document doc, String filePath) {
		super(doc, filePath);
	}
	
	@Override
	public String getType()
	{
		return TYPE;
	}
}
