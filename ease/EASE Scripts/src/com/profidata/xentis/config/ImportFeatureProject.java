package com.profidata.xentis.config;

public class ImportFeatureProject extends ImportProject {
	private final String contentPath;

	public static ImportFeatureProject of(String theName, String thePath, String theContentPath) {
		return new ImportFeatureProject(theName, thePath, theContentPath);
	}

	public ImportFeatureProject(String theName, String thePath, String theContentPath) {
		super(theName, thePath);
		contentPath = theContentPath;
	}

	public String getContentPath() {
		return contentPath;
	}
}
