package com.boomi.webmethods.node;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPathExpressionException;
import com.boomi.webmethods.util.Util;


import org.w3c.dom.Document;

public class AdapterService extends WMNode {

	public static final String TYPE="AdapterService";
	private Map<String,String> properties;
	public AdapterService(Document doc, String filePath) {
		super(doc, filePath);
		// TODO Auto-generated constructor stub
	}
	
	public Map<String,String> getProperties()
	{
		properties = new TreeMap<String,String>();
		properties.put("serviceTemplateName", super.findEncodedValue("serviceTemplateName"));
		properties.put("connectionName", super.findEncodedValue("connectionName"));
		
		if (findEncodedValue("serviceTemplateName").contentEquals("com.wm.adapter.wmjdbc.services.CustomSQL"))
		{
			properties.put("sql", super.findEncodedValue("sql"));
			properties.put("sqlFieldType", super.findEncodedValue("sqlFieldType"));
		} else {
			properties.put("transactionType", super.findEncodedValue("transactionType"));
			properties.put("datasourceClass", super.findEncodedValue("datasourceClass"));
			properties.put("serverName", super.findEncodedValue("user"));
			properties.put("user", super.findEncodedValue("sqlFieldType"));
			properties.put("password", super.findEncodedValue("password"));
			properties.put("databaseName", super.findEncodedValue("databaseName"));
			properties.put("portNumber", super.findEncodedValue("portNumber"));
		}
		return properties;
	}
	
	@Override
	public List<String> getDependencies()
	{
		List<String> list = super.getDependencies();
		String connection = this.findEncodedValue("connectionName");
		if (connection.length()>0)
			list.add(connection);
		return list;
	}
	
	@Override
	public String getType()
	{
		return TYPE;
	}
	
	// <value name="node_comment"></value>
//	generateDBInsert("db_insert_node.ndf");
	@Override
	public String getBoomiArtifact()
	{
		switch (properties.get("serviceTemplateName"))
		{
		case "com.wm.adapter.wmjdbc.services.CustomSQL":
			// connectionName LogistiCare.db:mySQLdb templateProperties   sql select * from VSTSLSD where VSTWHS = ? sqlFieldType java.lang.String colInfo  
		case "com.wm.adapter.wmjdbc.services.DynamicSQL":
			//connectionName LogistiCare.db:mySQLdb templateProperties   sql select * from VSTSLSD where VSTWHS = ? sqlFieldType java.lang.String sqlOutputField SQLStatement sqlVariables   sqlInputFields   sqlInputFieldTypes   inputJDBCType   CHAR inputFieldType    inputField   warehouse outputJDBCType   ARRAY outputFieldType   java.sql.Array resultFieldType   java.sql.Array[] outputField   fields resultField   results[].fields realOutputField  8 maxRow 0 queryTimeOut -1 resultRowField  resultRowFieldType java.lang.Integer userid overrideCredentials.$dbUser useridType  inputUseridSignF password overrideCredentials.$dbPassword passwordType  inputPasswordSignL designTimeLocale en_US  adapterServiceNodeVersion    adapterType JDBCAdapter inputRecordName dynamicInvoiceSQLInput outputRecordName dynamicInvoiceSQLOutput 
		case "com.wm.adapter.wmjdbc.services.Insert":
		case "com.wm.adapter.wmjdbc.services.BatchInsert":
		case "com.wm.adapter.wmjdbc.services.StoredProcedure":
			//connectionName LogistiCare.db:mySQLdb templateProperties   procedure.catalogName <current catalog> procedure.schemaName <current schema> procedure.autoRetrieve TRUE procedure.procedureName new_procedure procedure.doLocalPublish false procedure.returnFieldJDBCType  java.lang.String ARRAY procedure.returnFieldName   returnList procedure.queryTimeOut -1 params.paramJDBCType   params.paramName   params.paramInOutType   params.expression   params.inputField   params.inputFieldJDBCType   params.inputType   params.inputIndex   params.inputName   params.outputField    params.outputFieldJDBCType    params.outputType   java.sql.Array params.outputIndex   0 params.outputName    results.resultSetIndex   1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 results.resultSetName   column1 column2 column3                  results.oraRefCursorIndex   results.oraRefCursorName   results.oraRefCursorJDBCType   results.oraRefCursorJAVAType   results.resultName  ghi results.columnName      results.columnJDBCType      results.columnJAVAType  DDD results.outputField   column1[].returnList column2[].returnList column3[].returnList results.outputName      results.outputType   java.sql.Array[]   userid overrideCredentials.$dbUser useridType  inputUseridSign  password overrideCredentials.$dbPassword passwordType  inputPasswordSign  designTimeLocale en_US  adapterServiceNodeVersion    adapterType JDBCAdapter inputRecordName new_adapterServiceInput outputRecordName new_adapterServiceOutput 
		case "com.wm.adapter.wmjdbc.services.Select":
			//connectionName LogistiCare.db:mySQLdb templateProperties   tables.tableIndexes  java.lang.String t1 tables.catalogName   <current catalog> tables.schemaName   <current schema> tables.tableName   VSTSLSD tables.tableType   TABLE tables.columnInfo     select.sortOrder  %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% select.maxRow 0 select.queryTimeOut -1 select.resultRowField% select.resultRowFieldType  select.notifyOnUpdate   select.outputValueTypes   select.autoDelete false where.andOr  % where.leftParen  % where.leftExpr  : where.operator   = where.rightExpr   ? where.rightParen  % where.hiddenJDBCType    where.hiddenInputType   12 where.hiddenInputField                                                   where.hiddenInputFieldName   VSTWHS_1 where.JDBCType    where.inputType    where.inputField   warehouse userid overrideCredentials.$dbUser useridType  inputUseridSign                                                                               designTimeLocale en_US  adapterServiceNodeVersion    adapterType JDBCAdapter inputRecordName selectInvoicesInput outputRecordName selectInvoicesOutput 
		case "com.wm.adapter.wmjdbc.services.Update":
			//connectionName LogistiCare.db:mySQLdb templateProperties   tables.tableIndexes  java.lang.String  tables.catalogName   <current catalog> tables.schemaName   <current schema> tables.tableName   VSTSLSD tables.tableType   TABLE tables.columnInfo                                                                  
		case "com.wm.adapter.wmjdbc.services.Delete":

			break;
		}
		return "";
	}
	
	private static void generateDBInsert(String artifactPath) throws TransformerException, XPathExpressionException, IOException
	{
        TransformerFactory factory = TransformerFactory.newInstance();
        String xsl = Util.readFile("resources/webmethods2db_insert.xsl");
        Source xslt = new StreamSource(new ByteArrayInputStream(xsl.getBytes()));
        Transformer transformer = factory.newTransformer(xslt);

        Source text = new StreamSource(new File(artifactPath));
        transformer.transform(text, new StreamResult(new File("resources/dbinsert.xml")));
	}
}
