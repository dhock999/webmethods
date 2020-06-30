package com.boomi.manywho.services.projectanalyzer.fileservice;

import com.boomi.manywho.services.projectanalyzer.ApplicationConfiguration;
import com.boomi.webmethods.ProjectAnalyzer;
import com.boomi.webmethods.util.Util;
import com.google.common.collect.Lists;
import com.manywho.sdk.api.run.elements.type.FileListFilter;
import com.manywho.sdk.services.files.FileHandler;
import com.manywho.sdk.services.files.FileUpload;
import com.manywho.sdk.services.types.system.$File;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public class FileService implements FileHandler<ApplicationConfiguration> {
    @Override
    public List<$File> findAll(ApplicationConfiguration configuration, FileListFilter listFilter, String path) {
        return Lists.newArrayList(
                new $File(
                        "ff69eb79-1172-442f-953b-36c55d188e0d",
                        "Some kind",
                        "image/jpeg",
                        "File 1.jpg",
                        OffsetDateTime.parse("2018-12-25T01:23:34Z"),
                        OffsetDateTime.parse("2019-01-01T12:34:56Z"),
                        "This is File 1",
                        "https://example.com/download/File 1.jpg",
                        "https://example.com/embed/File 1.jpg",
                        "https://example.com/icon/File 1.jpg"
                ),
                new $File(
                        "6a2c044b-f294-4b77-9e05-882cf3423f2e",
                        "Another kind",
                        "text/plain",
                        "File 2.gif",
                        OffsetDateTime.parse("2017-12-25T01:23:34Z"),
                        OffsetDateTime.parse("2018-01-01T12:34:56Z"),
                        "This is File 2",
                        "https://example.com/download/File 2.doc",
                        "https://example.com/embed/File 2.doc",
                        "https://example.com/icon/File 2.doc"
                )
        );
    }

    @Override
    public $File upload(ApplicationConfiguration configuration, String path, FileUpload upload) {
    	String dictHTML="";
    	String reportHTML="";
    	try {
    		ProjectAnalyzer projectAnalyzer = new ProjectAnalyzer(upload.getContent());
//    		String dictActual = projectAnalyzer.getDictionaryXML();
    		dictHTML = projectAnalyzer.getDictionaryHTML();
    		reportHTML = projectAnalyzer.getDependencyReport();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

    	FileResponse file = new FileResponse(
                UUID.randomUUID().toString(),
                "Some kind",
                "text/plain",
                String.format("%s/%s", path, upload.getName()),
                OffsetDateTime.now(),
                OffsetDateTime.now(),
                "This is some description",
                String.format("https://example.com/download/%s", upload.getName()),
                String.format("https://example.com/embed/%s", upload.getName()),
                String.format("https://example.com/icon/%s", upload.getName())
                
        );
    	file.setDictHTML(dictHTML);
    	file.setReportHTML(reportHTML);
        return file;
    }
}
