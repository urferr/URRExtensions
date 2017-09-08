package com.profidata.xentis.config;

import java.util.HashMap;
import java.util.Map;

public class URRImportConfiguration implements ImportConfiguration {
	private static final ImportConfiguration instance = new URRImportConfiguration();

	private final String rootProjectPath = "";
	private final Map<String, ImportFeatureProject> features;

	public static ImportConfiguration getInstance() {
		return instance;
	}

	private URRImportConfiguration() {
		features = new HashMap<>();
		configureFeatures();
	}

	@Override
	public String getRootProjectPath() {
		return rootProjectPath;
	}

	@Override
	public ImportFeatureProject getFeatureProject(String theProjectName) {
		return features.get(theProjectName);
	}

	private void configureFeatures() {
		// Xentis unit test features
		features.put("_com.profidata.xentis.test.unit.feature", ImportFeatureProject.of("_com.profidata.xentis.test.unit.feature", "URRExtensions/features", ""));

		// Xentis integration test features
		features.put("_com.profidata.xentis.test.integration.feature", ImportFeatureProject.of("_com.profidata.xentis.test.integration.feature", "URRExtensions/features", ""));

		// XC unit test features
		features.put("_com.profidata.xc.one.test.unit.xnife.feature", ImportFeatureProject.of("_com.profidata.xc.one.test.unit.xnife.feature", "URRExtensions/features", ""));
		features.put("_com.profidata.xc.one.test.unit.platform.feature", ImportFeatureProject.of("_com.profidata.xc.one.test.unit.platform.feature", "URRExtensions/features", ""));

		// XC integration test features
		features.put("_com.profidata.xc.one.test.integration.platform.feature", ImportFeatureProject.of("_com.profidata.xc.one.test.integration.platform.feature", "URRExtensions/features", "xentis/xc_pltf/test"));
		features.put("_com.profidata.xc.one.test.integration.frontoffice.feature", ImportFeatureProject.of("_com.profidata.xc.one.test.integration.frontoffice.feature", "URRExtensions/features", "xentis/xc_fo/test"));

		// XC master test feature
		features.put("_com.profidata.xc.one.test.feature", ImportFeatureProject.of("_com.profidata.xc.one.test.feature", "URRExtensions/features", "URRExtensions/features"));
	}
}