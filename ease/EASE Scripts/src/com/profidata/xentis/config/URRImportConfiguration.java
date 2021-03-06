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
		// XC server features
		features.put("_com.profidata.xc.one.server.xnife.feature", ImportFeatureProject.of("_com.profidata.xc.one.server.xnife.feature", "URRExtensions/features", ""));
		features.put("_com.profidata.xc.one.server.platform.feature", ImportFeatureProject.of("_com.profidata.xc.one.server.platform.feature", "URRExtensions/features", ""));
		features.put("_com.profidata.xc.one.server.risk.feature", ImportFeatureProject.of("_com.profidata.xc.one.server.risk.feature", "URRExtensions/features", ""));
		features.put("_com.profidata.xc.one.server.backoffice.feature", ImportFeatureProject.of("_com.profidata.xc.one.server.backoffice.feature", "URRExtensions/features", ""));
		features.put("_com.profidata.xc.one.server.middleoffice.feature", ImportFeatureProject.of("_com.profidata.xc.one.server.middleoffice.feature", "URRExtensions/features", ""));
		features.put("_com.profidata.xc.one.server.frontoffice.feature", ImportFeatureProject.of("_com.profidata.xc.one.server.frontoffice.feature", "URRExtensions/features", ""));
		features.put("_com.profidata.xc.one.server.customizingconsole.feature", ImportFeatureProject.of("_com.profidata.xc.one.server.customizingconsole.feature", "URRExtensions/features", ""));
		features.put("_com.profidata.xc.one.server.feature", ImportFeatureProject.of("_com.profidata.xc.one.server.feature", "URRExtensions/features", ""));

		// XC client features
		features.put("_com.profidata.xc.one.client.xnife.feature", ImportFeatureProject.of("_com.profidata.xc.one.client.xnife.feature", "URRExtensions/features", ""));
		features.put("_com.profidata.xc.one.client.platform.feature", ImportFeatureProject.of("_com.profidata.xc.one.client.platform.feature", "URRExtensions/features", ""));
		features.put("_com.profidata.xc.one.client.risk.feature", ImportFeatureProject.of("_com.profidata.xc.one.client.risk.feature", "URRExtensions/features", ""));
		features.put("_com.profidata.xc.one.client.backoffice.feature", ImportFeatureProject.of("_com.profidata.xc.one.client.backoffice.feature", "URRExtensions/features", ""));
		features.put("_com.profidata.xc.one.client.middleoffice.feature", ImportFeatureProject.of("_com.profidata.xc.one.client.middleoffice.feature", "URRExtensions/features", ""));
		features.put("_com.profidata.xc.one.client.frontoffice.feature", ImportFeatureProject.of("_com.profidata.xc.one.client.frontoffice.feature", "URRExtensions/features", ""));
		features.put("_com.profidata.xc.one.client.customizingconsole.feature", ImportFeatureProject.of("_com.profidata.xc.one.client.customizingconsole.feature", "URRExtensions/features", ""));
		features.put("_com.profidata.xc.one.client.feature", ImportFeatureProject.of("_com.profidata.xc.one.client.feature", "URRExtensions/features", ""));

		// XC test features
		features.put("_com.profidata.xc.one.test.xnife.feature", ImportFeatureProject.of("_com.profidata.xc.one.test.xnife.feature", "URRExtensions/features", ""));
		features.put("_com.profidata.xc.one.test.platform.feature", ImportFeatureProject.of("_com.profidata.xc.one.test.platform.feature", "URRExtensions/features", ""));
		features.put("_com.profidata.xc.one.test.risk.feature", ImportFeatureProject.of("_com.profidata.xc.one.test.risk.feature", "URRExtensions/features", ""));
		features.put("_com.profidata.xc.one.test.backoffice.feature", ImportFeatureProject.of("_com.profidata.xc.one.test.backoffice.feature", "URRExtensions/features", ""));
		features.put("_com.profidata.xc.one.test.middleoffice.feature", ImportFeatureProject.of("_com.profidata.xc.one.test.middleoffice.feature", "URRExtensions/features", ""));
		features.put("_com.profidata.xc.one.test.frontoffice.feature", ImportFeatureProject.of("_com.profidata.xc.one.test.frontoffice.feature", "URRExtensions/features", ""));
		features.put("_com.profidata.xc.one.test.customizingconsole.feature", ImportFeatureProject.of("_com.profidata.xc.one.test.customizingconsole.feature", "URRExtensions/features", ""));

		// XC integration test features
		features.put("_com.profidata.xc.one.integration.test.feature", ImportFeatureProject.of("_com.profidata.xc.one.integration.test.feature", "URRExtensions/features", ""));

		// XC master test feature
		features.put("_com.profidata.xc.one.test.feature", ImportFeatureProject.of("_com.profidata.xc.one.test.feature", "URRExtensions/features", "URRExtensions/features"));

		// XC master ide features
		features.put("_com.profidata.xc.one.server.ide.feature", ImportFeatureProject.of("_com.profidata.xc.one.server.ide.feature", "URRExtensions/features", "URRExtensions/features"));
		features.put("_com.profidata.xc.one.client.ide.feature", ImportFeatureProject.of("_com.profidata.xc.one.client.ide.feature", "URRExtensions/features", "URRExtensions/features"));
	}
}