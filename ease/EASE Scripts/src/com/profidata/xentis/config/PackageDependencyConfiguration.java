package com.profidata.xentis.config;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PackageDependencyConfiguration {
	public static final Map<String, Set<String>> additionalBundlePackageDependencies;

	static {
		Set<String> somePackages;

		additionalBundlePackageDependencies = new HashMap<>();

		somePackages = new HashSet<>();
		somePackages.add("org.springframework.beans");
		somePackages.add("org.springframework.beans.factory");
		somePackages.add("org.springframework.core.io.support");
		additionalBundlePackageDependencies.put("com.profidata.xentis.env.shared", somePackages);

		somePackages = new HashSet<>();
		somePackages.add("org.springframework.context");
		somePackages.add("org.springframework.jms.support");
		somePackages.add("com.profidata.xentis.jni.common");
		additionalBundlePackageDependencies.put("com.profidata.xentis.jms.shared", somePackages);
	}

	public static final Map<String, Set<String>> additionalTestFragmentPackageDependencies;

	static {
		Set<String> somePackages;

		additionalTestFragmentPackageDependencies = new HashMap<>();

		somePackages = new HashSet<>();
		somePackages.add("com.profidata.xentis.domain.unified");
		somePackages.add("com.xnife.domain");
		additionalTestFragmentPackageDependencies.put("com.profidata.risk.commons.test", somePackages);

		somePackages = new HashSet<>();
		somePackages.add("org.objenesis");
		additionalTestFragmentPackageDependencies.put("com.profidata.xentis.bodomain.test", somePackages);

		somePackages = new HashSet<>();
		somePackages.add("org.objenesis");
		somePackages.add("org.apache.commons.logging");
		somePackages.add("org.springframework.aop");
		somePackages.add("org.springframework.expression");
		additionalTestFragmentPackageDependencies.put("com.profidata.xentis.env.shared.test", somePackages);

		somePackages = new HashSet<>();
		somePackages.add("org.objenesis");
		additionalTestFragmentPackageDependencies.put("com.profidata.xentis.etl.commons.test", somePackages);

		somePackages = new HashSet<>();
		somePackages.add("org.objenesis");
		somePackages.add("com.profidatagroup.xentis.ui.base.lookandfeel");
		additionalTestFragmentPackageDependencies.put("com.profidata.xentis.javamis.test", somePackages);

		somePackages = new HashSet<>();
		somePackages.add("org.springframework.context");
		somePackages.add("org.objenesis");
		somePackages.add("javax.management.j2ee.statistics");
		additionalTestFragmentPackageDependencies.put("com.profidata.xentis.jms.shared.test", somePackages);

		somePackages = new HashSet<>();
		somePackages.add("org.springframework.expression");
		additionalTestFragmentPackageDependencies.put("com.xnife.spring.test", somePackages);

		somePackages = new HashSet<>();
		somePackages.add("org.springframework.context");
		additionalTestFragmentPackageDependencies.put("com.profidata.xentis.env.client.test", somePackages);

		somePackages = new HashSet<>();
		somePackages.add("org.springframework.context");
		additionalBundlePackageDependencies.put("com.profidata.xc.risk.service.impl.test", somePackages);

		somePackages = new HashSet<>();
		somePackages.add("org.powermock.modules.junit4.common.internal.impl");
		somePackages.add("org.powermock.api.support.membermodification");
		somePackages.add("org.mockito.stubbing");
		somePackages.add("net.bytebuddy.dynamic.loading");
		somePackages.add("org.objenesis");
		additionalBundlePackageDependencies.put("com.compxc.order.presentation.test", somePackages);
	}

	private PackageDependencyConfiguration() {
		// no instance needed
	}
}
