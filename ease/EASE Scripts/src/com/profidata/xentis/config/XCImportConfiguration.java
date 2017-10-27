package com.profidata.xentis.config;

import java.util.HashMap;
import java.util.Map;

public class XCImportConfiguration implements ImportConfiguration {
	private static final ImportConfiguration instance = new XCImportConfiguration();

	private final String rootProjectPath = "xentisjava";
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
		features.put("_com.compxc.eclipse.feature", ImportFeatureProject.of("_com.compxc.eclipse.feature", "xc-pde", null));
		features.put("_com.compxc.eclipse.rcp.feature", ImportFeatureProject.of("_com.compxc.eclipse.rcp.feature", "xc-pde", null));
		features.put("_com.compxc.eclipse4.spy.feature", ImportFeatureProject.of("_com.compxc.eclipse4.spy.feature", "xc-pde", null));
		features.put("_com.compxc.eclipse.test.feature", ImportFeatureProject.of("_com.compxc.eclipse.test.feature", "xc-pde", null));

		// XC xentis features
		features.put("_com.compxc.xentis.feature", ImportFeatureProject.of("_com.compxc.xentis.feature", "xc-pde", null));
		features.put("_com.compxc.xentis.rcp.feature", ImportFeatureProject.of("_com.compxc.xentis.rcp.feature", "xc-pde", null));
	}
}