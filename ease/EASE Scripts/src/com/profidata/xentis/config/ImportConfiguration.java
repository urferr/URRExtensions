package com.profidata.xentis.config;

import java.util.HashMap;
import java.util.Map;

public class ImportConfiguration {
	public static class ImportProject {
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

	public static class ImportFeatureProject extends ImportProject {
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

	private static final ImportConfiguration instance = new ImportConfiguration();

	private final String xentisRootProjectPath = "xentis";
	private final Map<String, ImportFeatureProject> features;

	public static ImportConfiguration getInstance() {
		return instance;
	}

	private ImportConfiguration() {
		features = new HashMap<>();
		configureFeatures();
	}

	public String getXentisRootProjectPath() {
		return xentisRootProjectPath;
	}

	public ImportFeatureProject getFeatureProject(String theProjectName) {
		return features.get(theProjectName);
	}

	private void configureFeatures() {
		// XC thirdparty features (content does not have to be imported)
		features.put("_com.compxc.eclipse.feature", ImportFeatureProject.of("_com.compxc.eclipse.feature", "xc_bld", null));
		features.put("_com.compxc.eclipse.rcp.feature", ImportFeatureProject.of("_com.compxc.eclipse.rcp.feature", "xc_bld", null));
		features.put("_com.compxc.eclipse4.spy.feature", ImportFeatureProject.of("_com.compxc.eclipse4.spy.feature", "xc_bld", null));
		features.put("_com.compxc.eclipse.test.feature", ImportFeatureProject.of("_com.compxc.eclipse.test.feature", "xc_bld", null));

		// XC Xentis features (built and imported by Gradle)
		features.put("_com.compxc.xentis.feature", ImportFeatureProject.of("_com.compxc.xentis.feature", "xc_bld", null));
		features.put("_com.compxc.xentis.rcp.feature", ImportFeatureProject.of("_com.compxc.xentis.rcp.feature", "xc_bld", null));

		// XC server features
		features.put("_com.profidata.xc.one.server.xnife.feature", ImportFeatureProject.of("_com.profidata.xc.one.server.xnife.feature", "xc_pltf/main", "xnife/main"));
		features.put("_com.profidata.xc.one.server.platform.feature", ImportFeatureProject.of("_com.profidata.xc.one.server.platform.feature", "xc_pltf/main", "xc_pltf/main"));
		features.put("_com.profidata.xc.one.server.risk.feature", ImportFeatureProject.of("_com.profidata.xc.one.server.risk.feature", "xrs", "xrs"));
		features.put("_com.profidata.xc.one.server.backoffice.feature", ImportFeatureProject.of("_com.profidata.xc.one.server.backoffice.feature", "JavAMIS", "JavAMIS"));
		features.put("_com.profidata.xc.one.server.middleoffice.feature", ImportFeatureProject.of("_com.profidata.xc.one.server.middleoffice.feature", "xc_mo/main", "xc_mo/main"));
		features.put("_com.profidata.xc.one.server.frontoffice.feature", ImportFeatureProject.of("_com.profidata.xc.one.server.frontoffice.feature", "xc_fo/main", "xc_fo/main"));
		features.put(
				"_com.profidata.xc.one.server.frontoffice.incubator.securities.feature",
				ImportFeatureProject.of("_com.profidata.xc.one.server.frontoffice.incubator.securities.feature", "xc_fo/incubator", "xc_fo/incubator"));
		features.put("_com.profidata.xc.one.server.customizingconsole.feature", ImportFeatureProject.of("_com.profidata.xc.one.server.customizingconsole.feature", "xc_cc/main", "xc_cc/main"));
		features.put("_com.profidata.xc.one.server.feature", ImportFeatureProject.of("_com.profidata.xc.one.server.feature", "xc_one/main", "xc_one/main"));

		// XC client features
		features.put("_com.profidata.xc.one.client.xnife.feature", ImportFeatureProject.of("_com.profidata.xc.one.client.xnife.feature", "xc_pltf/main", "xnife/main"));
		features.put("_com.profidata.xc.one.client.platform.feature", ImportFeatureProject.of("_com.profidata.xc.one.client.platform.feature", "xc_pltf/main", "xc_pltf/main"));
		features.put("_com.profidata.xc.one.client.risk.feature", ImportFeatureProject.of("_com.profidata.xc.one.client.risk.feature", "xrs", "xrs"));
		features.put("_com.profidata.xc.one.client.backoffice.feature", ImportFeatureProject.of("_com.profidata.xc.one.client.backoffice.feature", "JavAMIS", "JavAMIS"));
		features.put("_com.profidata.xc.one.client.middleoffice.feature", ImportFeatureProject.of("_com.profidata.xc.one.client.middleoffice.feature", "xc_mo/main", "xc_mo/main"));
		features.put("_com.profidata.xc.one.client.frontoffice.feature", ImportFeatureProject.of("_com.profidata.xc.one.client.frontoffice.feature", "xc_fo/main", "xc_fo/main"));
		features.put(
				"_com.profidata.xc.one.client.frontoffice.incubator.securities.feature",
				ImportFeatureProject.of("_com.profidata.xc.one.client.frontoffice.incubator.securities.feature", "xc_fo/incubator", "xc_fo/incubator"));
		features.put("_com.profidata.xc.one.client.customizingconsole.feature", ImportFeatureProject.of("_com.profidata.xc.one.client.customizingconsole.feature", "xc_cc/main", "xc_cc/main"));
		features.put("_com.profidata.xc.one.client.feature", ImportFeatureProject.of("_com.profidata.xc.one.client.feature", "xc_one/main", "xc_one/main"));

		// XC unit test features
		features.put("_com.profidata.xc.one.test.xnife.feature", ImportFeatureProject.of("_com.profidata.xc.one.test.xnife.feature", "xc_pltf/test", "xnife/main"));
		features.put("_com.profidata.xc.one.test.unit.platform.feature", ImportFeatureProject.of("_com.profidata.xc.one.test.unit.platform.feature", "xc_pltf/test", "xc_pltf/test"));
		features.put("_com.profidata.xc.one.test.unit.risk.feature", ImportFeatureProject.of("_com.profidata.xc.one.test.unit.risk.feature", "xrs", "xrs"));
		features.put("_com.profidata.xc.one.test.unit.backoffice.feature", ImportFeatureProject.of("_com.profidata.xc.one.test.unit.backoffice.feature", "JavAMIS", "JavAMIS"));
		features.put("_com.profidata.xc.one.test.unit.middleoffice.feature", ImportFeatureProject.of("_com.profidata.xc.one.test.unit.middleoffice.feature", "xc_mo/test", "xc_mo/test"));
		features.put("_com.profidata.xc.one.test.unit.frontoffice.feature", ImportFeatureProject.of("_com.profidata.xc.one.test.unit.frontoffice.feature", "xc_fo/test", "xc_fo/test"));
		features.put("_com.profidata.xc.one.test.unit.customizingconsole.feature", ImportFeatureProject.of("_com.profidata.xc.one.test.unit.customizingconsole.feature", "xc_cc/test", "xc_cc/test"));

		// XC integration test features
		features.put("_com.profidata.xc.one.test.integration.platform.feature", ImportFeatureProject.of("_com.profidata.xc.one.test.integration.platform.feature", "xc_pltf/test", "xc_pltf/test"));
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