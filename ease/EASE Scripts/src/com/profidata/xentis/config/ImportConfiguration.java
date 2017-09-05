package com.profidata.xentis.config;

public interface ImportConfiguration {

	String getRootProjectPath();

	ImportFeatureProject getFeatureProject(String theProjectName);

}