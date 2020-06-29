package com.boomi.webmethods.node;

import java.io.ByteArrayInputStream;
import java.io.File;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

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
	
	@Override
	//TOFO puy this in flow.xml
//	generateXSD("flow.xml", "MAPTARGET"); 
//	generateXSD("flow.xml", "MAPSOURCE");

	public String getBoomiArtifact() 
	{
        TransformerFactory factory = TransformerFactory.newInstance();
        String xsl=null;// = readFile("webmethods2xsd.xsl");
        xsl = xsl.replaceFirst("%SOURCE_TARGET%", this.getName());
        Source xslt = new StreamSource(new ByteArrayInputStream(xsl.getBytes()));
        try {
			Transformer transformer = factory.newTransformer(xslt);
		} catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

//        Source text = new StreamSource(new File(artifactPath));
//        transformer.transform(text, new StreamResult(new File(elementName + ".xsd")));
        return "";
	}
}
