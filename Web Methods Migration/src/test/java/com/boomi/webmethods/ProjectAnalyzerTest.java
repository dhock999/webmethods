package com.boomi.webmethods;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.xml.sax.InputSource;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.DefaultNodeMatcher;
import org.xmlunit.diff.Diff;
import org.xmlunit.diff.Difference;
import org.xmlunit.diff.ElementSelectors;
import com.boomi.webmethods.util.Util;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;



public class ProjectAnalyzerTest {
	
	private static String integrationServerPath;
	private static FileOutputStream fosDump;
	ProjectAnalyzer pa;
    @BeforeEach
    void init() throws Exception {
    	pa = new ProjectAnalyzer(Util.getResourceAsStream("resources/myPackage.zip", this.getClass()));
    }

	
//    private static final Map<Integer, String> myMap = new HashMap<>();
//    static {
//        myMap.put(1, "one");
//        myMap.put(2, "two");
//    }	
	@Test
	void testGetDictionaryXML() throws Exception {
//		generateXSD("flow.xml", "MAPTARGET");
//		generateXSD("flow.xml", "MAPSOURCE");
//		generateDBInsert("db_insert_node.ndf");
		
		String actual = pa.getDictionaryXML();
		compareXML(actual, "getDictionaryXML", this.getClass(), false);
	}

	@Test
	void testGetDictionaryHTML() throws Exception {
		String actual = pa.getDictionaryHTML();
		compareXML(actual, "getDictionaryHTML", this.getClass(), false);
	}

	@Test
	void testGetDependencyReport() throws Exception {
		String actual = pa.getDependencyReport();
		compareXML(actual, "getDependencyReport", this.getClass(), false);
	}

	@Test
	void testStartCrawlZip() throws Exception
	{
		fosDump = new FileOutputStream(new File("dump.txt"));
        ZipInputStream zis = new ZipInputStream(Util.getResourceAsStream("resources/myPackage.zip", this.getClass()));
        crawlZip("", zis, zis.getNextEntry());		
        zis.close();
		fosDump.close();
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
	
	public static void compareXML(String actual, String testName, Class theClass, boolean writeExpected) throws Exception
	{
        System.out.println("");
        System.out.println(testName);
		if (writeExpected)
		{
			FileWriter writer = new FileWriter("src/test/java/resources/expected/"+testName+".xml");
			writer.write(actual);
			writer.flush();
			writer.close();
		}
        String expected = Util.readResource("resources/expected/"+testName+".xml", theClass);
        System.out.println(expected);       
        System.out.println(actual);       

	
       Diff myDiffSimilar;
       myDiffSimilar = DiffBuilder.compare(expected).withTest(actual)
	     .withNodeMatcher(new DefaultNodeMatcher(ElementSelectors.byName))
	     .checkForSimilar().ignoreWhitespace()
	     .build();
       	System.out.println("actual: " + actual.length() + " expected: " + expected.length());
        System.out.println(myDiffSimilar.toString());
        for (Difference dif: myDiffSimilar.getDifferences())
        {
            System.out.println("DIFFERENCE");
            System.out.println(dif.toString());
        }

        assertTrue(!myDiffSimilar.hasDifferences());
        System.out.println(myDiffSimilar.toString());
        assertTrue(!myDiffSimilar.hasDifferences());
        assertEquals(actual.length(), expected.length());
	}
}