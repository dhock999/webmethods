package com.boomi.webmethods;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.boomi.webmethods.flow.Flow;
import com.boomi.webmethods.node.WMNode;
import com.boomi.webmethods.node.WMNodeFactory;
import com.boomi.webmethods.util.Util;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;



public class ProjectAnalyzerTest {
	
	private static String integrationServerPath;
	private static FileOutputStream fosDump;
	
	
//    private static final Map<Integer, String> myMap = new HashMap<>();
//    static {
//        myMap.put(1, "one");
//        myMap.put(2, "two");
//    }	
	@Test
	void testProjectAnalyzer() throws Exception {
//		generateXSD("flow.xml", "MAPTARGET");
//		generateXSD("flow.xml", "MAPSOURCE");
//		generateDBInsert("db_insert_node.ndf");
		
		ProjectAnalyzer pa = new ProjectAnalyzer(Util.getResourceAsStream("myPackage.zip", this.getClass()));
		String dictActual = pa.getDictionaryXML();
		String dictHTMLActual = pa.getDictionaryHTML();
		String reportActual = pa.getDependencyReport();
		assertEquals(reportActual, "");

	}
	
	@Test
	void testStartCrawlZip() throws Exception
	{
		fosDump = new FileOutputStream(new File("dump.txt"));
        ZipInputStream zis = new ZipInputStream(Util.getResourceAsStream("myPackage.zip", this.getClass()));
        crawlZip("", zis, zis.getNextEntry());		
        zis.close();
		fosDump.close();
	}
	
	private static void copyStream(InputStream in, OutputStream out) throws IOException
	{
		byte[] buffer = new byte[1024];
		int len = in.read(buffer);
		while (len != -1) {
		    out.write(buffer, 0, len);
		    len = in.read(buffer);
		}
	}
	
	private static String readFile(String filePath) 
	{
	    StringBuilder contentBuilder = new StringBuilder();
	    try (Stream<String> stream = Files.lines( Paths.get(filePath), StandardCharsets.ISO_8859_1)) 
	    {
	        stream.forEach(s -> contentBuilder.append(s).append("\n"));
	    }
	    catch (IOException e) 
	    {
	        e.printStackTrace();
	    }
	    return contentBuilder.toString();
	}
	
	public static void crawlZip(String path, ZipInputStream zis, ZipEntry entry) throws IOException, XPathExpressionException
	{
        while(entry != null) {
        	String filePath = path+"/"+entry.getName();
//            System.out.println(entry.getName());
            if (entry.isDirectory()) {
                crawlZip(filePath, zis, zis.getNextEntry());
            } else {             
	           	if (entry.getName().contentEquals("node.ndf") || entry.getName().contentEquals("flow.xml"))
	           	{
	           		fosDump.write(("\r\n"+filePath.substring(integrationServerPath.length())+"\r\n").getBytes() );
//	           		copyStream(new FileInputStream(new File(f.getAbsolutePath())), fosDump);         		
	           	}
	           	if (entry.getName().contentEquals("node.ndf"))
	           		dumpIRTNODE(filePath);
            }
       
            zis.closeEntry();
            entry = zis.getNextEntry();
        }
	}
	
	private static String getIRTNode(String path) throws XPathExpressionException, IOException
	{
		String props="";
		XPath xpath = XPathFactory.newInstance().newXPath();
		InputSource inputSource = new InputSource(new FileInputStream(new File(path))); 
		String base64 = xpath.evaluate("Values/value[@name = 'IRTNODE_PROPERTY']", inputSource);
		if (base64==null || base64.length()==0)
		{
			inputSource = new InputSource(new FileInputStream(new File(path))); 
			base64 = xpath.evaluate("Values/value[@name = 'IDataEncoded']", inputSource);
			System.out.println(base64);
		}
		if (base64!=null && base64.length()>0)
		{
			byte bytes[] = Base64.getDecoder().decode(base64.trim().replaceAll("\n", ""));
			props = new String(bytes, StandardCharsets.UTF_16);
		}
		return props;
	}
	
	public static void dumpIRTNODE(String path) throws XPathExpressionException, IOException
	{
		String props = getIRTNode(path);
		if (props.length()>10)
		{
			fosDump.write("BASE64\r\n".getBytes());
			StringBuilder sb = new StringBuilder();
			for (int i=0; i<props.length(); i++)
			{
				if (props.charAt(i)<' ' || props.charAt(i)>'z')
					sb.append(" ");
				else
					sb.append(props.charAt(i));
			}
			fosDump.write(sb.toString().getBytes());		}
	}

	//TODO this is a KLUDGE to get name/value pairs from this binary/UTF_16 character string
	private static String findValue(String src, String tag)
	{
		StringBuilder value = new StringBuilder();
		int start = src.indexOf(tag);
		if (start>-1)
		{
			start+=tag.length()+1;
			start=findNextString(src, start);
			while (start>-1 && start<src.length() && src.charAt(start)>=' ' && src.charAt(start)<='z')
			{
				value.append(src.charAt(start));
				start++;
			}
		}
		
		return value.toString();
	}
	
	private static int findNextString(String src, int start)
	{
		while (start<src.length() && (src.charAt(start)<' ' || src.charAt(start)>'z'))
			start++;
		if (start>=src.length())
			start=-1;
		return start;
	}
}