package com.profidata.xentis.config;

import java.util.HashMap;
import java.util.Map;

public class XCImportConfiguration implements ImportConfiguration {
	private static final ImportConfiguration instance = new XCImportConfiguration();

	private final String rootProjectPath = "xentis";
	private final Map<String, ImportFeatureProject> features;

	public static ImportConfiguration getInstance() {
		return instance;
	}

	private XCImportConfiguration() {
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
		// XC thirdparty features (content does not have to be imported)
		features.put("_com.compxc.eclipse.feature", ImportFeatureProject.of("_com.compxc.eclipse.feature", "xc_bld", null));
		features.put("_com.compxc.eclipse.rcp.feature", ImportFeatureProject.of("_com.compxc.eclipse.rcp.feature", "xc_bld", null));
		features.put("_com.compxc.eclipse4.spy.feature", ImportFeatureProject.of("_com.compxc.eclipse4.spy.feature", "xc_bld", null));
		features.put("_com.compxc.eclipse.test.feature", ImportFeatureProject.of("_com.compxc.eclipse.test.feature", "xc_bld", null));

		// XC server features
		features.put(
				"_com.profidata.xc.one.server.frontoffice.incubator.securities.feature",
				ImportFeatureProject.of("_com.profidata.xc.one.server.frontoffice.incubator.securities.feature", "xc_fo/incubator", "xc_fo/incubator"));

		// XC client features
		features.put(
				"_com.profidata.xc.one.client.frontoffice.incubator.securities.feature",
				ImportFeatureProject.of("_com.profidata.xc.one.client.frontoffice.incubator.securities.feature", "xc_fo/incubator", "xc_fo/incubator"));

		// XC unit test features
		features.put("_com.profidata.xc.one.test.unit.frontoffice.feature", ImportFeatureProject.of("_com.profidata.xc.one.test.unit.frontoffice.feature", "xc_fo/test", "xc_fo/test"));
		features.put("_com.profidata.xc.one.test.unit.customizingconsole.feature", ImportFeatureProject.of("_com.profidata.xc.one.test.unit.customizingconsole.feature", "xc_cc/test", "xc_cc/test"));

		// XC integration test features
		features.put("_com.profidata.xc.one.test.integration.middleoffice.feature", ImportFeatureProject.of("_com.profidata.xc.one.test.integration.middleoffice.feature", "xc_mo/test", "xc_mo/test"));
		features.put("_com.profidata.xc.one.test.integration.frontoffice.feature", ImportFeatureProject.of("_com.profidata.xc.one.test.integration.frontoffice.feature", "xc_fo/test", "xc_fo/test"));
		features.put(
				"_com.profidata.xc.one.test.integration.frontoffice.incubator.feature",
				ImportFeatureProject.of("_com.profidata.xc.one.test.integration.frontoffice.incubator.feature", "xc_fo/incubator", "xc_fo/incubator"));

		// Xc build features
		features.put("_com.profidata.xc.one.server.build.feature", ImportFeatureProject.of("_com.profidata.xc.one.server.build.feature", "xc_bld", "xc_bld"));
		features.put("_com.profidata.xc.one.client.build.feature", ImportFeatureProject.of("_com.profidata.xc.one.client.build.feature", "xc_bld", "xc_bld"));
		features.put("_com.profidata.xc.one.test.build.feature", ImportFeatureProject.of("_com.profidata.xc.one.test.build.feature", "xc_bld", "xc_bld"));
		features.put("_com.profidata.xc.one.all.build.feature", ImportFeatureProject.of("_com.profidata.xc.one.all.build.feature", "xc_bld", "xc_bld"));
		features.put("_com.profidata.xc.one.all.build.incubator.feature", ImportFeatureProject.of("_com.profidata.xc.one.all.build.incubator.feature", "xc_bld", "xc_bld"));
	}
}