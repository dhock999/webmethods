package com.boomi.webmethods.node;

import java.util.List;
import org.w3c.dom.Document;

public class FlatFileSchema extends WMNode {

	public static final String TYPE="Flat File Schema";

	public FlatFileSchema(Document doc, String filePath) {
		super(doc, filePath);
	}

	@Override
	public List<String> getDependencies()
	{
		List<String> list = super.getDependencies();
		String dep = this.findEncodedValue("PartHolder");
		if (dep.length()>0)
			list.add(dep);
		return list;
	}
	
	@Override
	public String getType()
	{
		return TYPE;
	}
}
