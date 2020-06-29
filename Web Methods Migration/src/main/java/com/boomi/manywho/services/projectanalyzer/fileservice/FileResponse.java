package com.boomi.manywho.services.projectanalyzer.fileservice;

import java.time.OffsetDateTime;

import com.boomi.manywho.services.projectanalyzer.ApplicationConfiguration;
import com.manywho.sdk.api.ContentType;
import com.manywho.sdk.services.types.Type;
import com.manywho.sdk.services.types.system.$File;

@Type.Element(name = "Project Analyzer File Upload Response")
public class FileResponse extends $File {
	@Type.Property(name = "Dictionary HTML", contentType = ContentType.String)
	String dictHTML="";
	@Type.Property(name = "Dependency HTML", contentType = ContentType.String)
	String reportHTML="";
//	@Type.Property(name = "Flows", contentType = ContentType.List)
//	String flows="";
//	@Type.Property(name = "Services", contentType = ContentType.List)
//	String services="";
	
	FileResponse (String id, String kind, String name, String mimeType, OffsetDateTime dateCreated, OffsetDateTime dateModified, String description, String downloadUri, String embedUri, String iconUri) {
		super (id,kind,mimeType,name, dateCreated,dateModified,description,downloadUri, embedUri, iconUri);
	}

	public String getDictHTML() {
		return dictHTML;
	}

	public void setDictHTML(String dictHTML) {
		this.dictHTML = dictHTML;
	}

	public String getReportHTML() {
		return reportHTML;
	}

	public void setReportHTML(String reportHTML) {
		this.reportHTML = reportHTML;
	}
}