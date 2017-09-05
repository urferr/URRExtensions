package com.profidata.xentis.config;

public class ImportProject {
	private final String name;
	private final String path;

	protected ImportProject(String theName, String thePath) {
		name = theName;
		path = thePath;
	}

	public String getName() {
		return name;
	}

	public String getPath() {
		return path;
	}
}
