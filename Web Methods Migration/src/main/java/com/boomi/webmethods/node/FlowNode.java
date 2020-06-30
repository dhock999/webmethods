package com.boomi.webmethods.node;

import org.w3c.dom.Document;

public class FlowNode extends WMNode {

	public static final String TYPE="flow";

	public FlowNode(Document doc, String filePath) {
		super(doc, filePath);
	}
	
	@Override
	public String getType()
	{
		return TYPE;
	}
}
