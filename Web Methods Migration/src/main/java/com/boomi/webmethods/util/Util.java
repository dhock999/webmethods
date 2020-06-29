package com.boomi.webmethods.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Scanner;
import java.util.zip.ZipInputStream;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

public class Util {
	
    public static InputStream getResourceAsStream(String resourcePath, Class theClass) throws Exception
    {
    	InputStream is = null;
		try {
			is = theClass.getClassLoader().getResourceAsStream(resourcePath);			
		} catch (Exception e)
		{
			throw new Exception("Error loading resource: "+resourcePath + " " + e.getMessage());
		}
		if (is==null)
			throw new Exception("Error loading resource: "+resourcePath);
		return is;
    }
    
    //Read but do not close the input stream
    public static String zipInputStreamToString(InputStream zis) throws IOException
    {
    	StringBuilder sb = new StringBuilder();
		byte[] buffer = new byte[1024];
		int len = zis.read(buffer);
		while (len != -1) {
			
			String s = new String(buffer, "UTF-8");
			s=s.substring(0, len);
			sb.append(s);
		    len = zis.read(buffer);
		}
		return sb.toString();
    }
    
    public static String inputStreamToString(InputStream is) throws IOException
    {
    	try (Scanner scanner = new Scanner(is, "UTF-8")) {
    		return scanner.useDelimiter("\\A").next();
    	}
    }
    
    public static String readResource(String resourcePath, Class theClass) throws Exception
	{
		String resource = null;
		try {
			resource = inputStreamToString(getResourceAsStream(resourcePath, theClass));
			
		} catch (Exception e)
		{
			throw new Exception("Error loading resource: "+resourcePath + " " + e.getMessage());
		}
		return resource;
	}
	
	public static String docToString(Document doc) {
	    try {
	        StringWriter sw = new StringWriter();
	        TransformerFactory tf = TransformerFactory.newInstance();
	        Transformer transformer = tf.newTransformer();
	        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
	        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
	        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

	        transformer.transform(new DOMSource(doc), new StreamResult(sw));
	        return sw.toString();
	    } catch (Exception ex) {
	        throw new RuntimeException("Error converting to String", ex);
	    }
	}
}