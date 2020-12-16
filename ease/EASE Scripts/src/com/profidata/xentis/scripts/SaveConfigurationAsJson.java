package com.profidata.xentis.scripts;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.profidata.eclipse.project.model.fix.AdditionalProjectConfigurations;
import com.profidata.eclipse.project.model.fix.AdditionalProjectConfigurations.ClasspathEntry;
import com.profidata.eclipse.project.model.fix.AdditionalProjectConfigurations.ClasspathEntry.ClasspathEntryType;
import com.profidata.eclipse.project.model.fix.AdditionalProjectConfigurations.ProjectConfiguration;

public class SaveConfigurationAsJson {
	private static PrintStream output;
	private static PrintStream error;

	private final List<String> someAdditionalTestRuntimeBundles = List.of(
			"net.bytebuddy.byte-buddy",
			"org.objenesis",
			"ch.qos.logback.classic",
			"ch.qos.logback.core");

	public static void initialize(InputStream theInput, PrintStream theOutput, PrintStream theError) {
		SaveConfigurationAsJson.output = theOutput;
		SaveConfigurationAsJson.error = theError;
	}

	public static void main(String[] args) throws Exception {
		new SaveConfigurationAsJson().execute();
	}

	private void execute() {
		AdditionalProjectConfigurations aConfiguration = createConfig("JavaSE-11", "org.eclipse.jdt.junit.JUNIT_CONTAINER/5");
		IPath aAdditionalProjectConfigurationPath = ResourcesPlugin.getWorkspace().getRoot().getLocation().append("URRExtensions/ease/EASE Scripts").append("AdditionalProjectConfiguration.json");
		Gson gson = new GsonBuilder().setPrettyPrinting().create();

		// 1. Java object to JSON, and save into a file
		try (Writer aWriter = new FileWriter(aAdditionalProjectConfigurationPath.toOSString())) {
			gson.toJson(aConfiguration, aWriter);
		}
		catch (JsonIOException | IOException cause) {
			output.println("could not write json file: " + aAdditionalProjectConfigurationPath.toOSString());
		}

		aConfiguration = null;
		try (Reader aReader = new FileReader(aAdditionalProjectConfigurationPath.toOSString())) {
			aConfiguration = gson.fromJson(aReader, AdditionalProjectConfigurations.class);
		}
		catch (JsonIOException | IOException cause) {
			output.println("could not read json file: " + aAdditionalProjectConfigurationPath.toOSString());
		}

		// 2. Java object to JSON, and assign to a String
		String jsonInString = gson.toJson(aConfiguration);

		output.println(jsonInString);
	}

	private AdditionalProjectConfigurations createConfig(String theExecutionEnvironment, String theJUnitLibraryPath) {
		AdditionalProjectConfigurations aProjectConfiguration = new AdditionalProjectConfigurations(theExecutionEnvironment, theJUnitLibraryPath);

		aProjectConfiguration.projectConfigurations.putAll(getAdditionalBundleConfigurations());
		aProjectConfiguration.projectConfigurations.putAll(getAdditionalTestFragmentConfigurations());

		return aProjectConfiguration;
	}

	private Map<String, ProjectConfiguration> getAdditionalBundleConfigurations() {
		Map<String, ProjectConfiguration> allBundles = new HashMap<>();
		ProjectConfiguration aConfiguration;

		aConfiguration = new ProjectConfiguration("UTF-8");
		aConfiguration.additionalClasspathEntries.add(new ClasspathEntry(ClasspathEntryType.Library, "provided/atdl4j-swing-with-dependencies.jar"));
		aConfiguration.additionalClasspathEntries.add(new ClasspathEntry(ClasspathEntryType.Library, "provided/foxtrot-core.jar"));
		aConfiguration.additionalClasspathEntries.add(new ClasspathEntry(ClasspathEntryType.Container, "org.eclipse.jdt.launching.JRE_CONTAINER/org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType/JavaSE-11"));
		allBundles.put("com.profidata.xentis.javamis.client", aConfiguration);

		aConfiguration = new ProjectConfiguration("UTF-8");
		aConfiguration.additionalClasspathEntries.add(new ClasspathEntry(ClasspathEntryType.Project, "com.profidata.xentis.session"));
		aConfiguration.additionalClasspathEntries.add(new ClasspathEntry(ClasspathEntryType.Container, "org.eclipse.jdt.launching.JRE_CONTAINER/org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType/JavaSE-11"));
		allBundles.put("com.profidata.xentis.javamis.shared", aConfiguration);

		aConfiguration = new ProjectConfiguration("UTF-8");
		allBundles.put("com.profidata.xentis.javamisxc", aConfiguration);

		aConfiguration = new ProjectConfiguration(null);
		aConfiguration.additionalClasspathEntries.add(new ClasspathEntry(ClasspathEntryType.Library, "antlr-runtime-3.2.jar"));
		allBundles.put("com.objectquery.plan.parser", aConfiguration);

		return allBundles;
	}

	private Map<String, ProjectConfiguration> getAdditionalTestFragmentConfigurations() {
		Map<String, ProjectConfiguration> allBundles = new HashMap<>();

		allBundles.putAll(getAdditionalXnifeTestFragmentConfigurations());
		allBundles.putAll(getAdditionalPlatformTestFragmentConfigurations());
		allBundles.putAll(getAdditionalRiskTestFragmentConfigurations());
		allBundles.putAll(getAdditionalMiddleOfficeTestFragmentConfigurations());
		allBundles.putAll(getAdditionalFrontOfficeTestFragmentConfigurations());
		allBundles.putAll(getAdditionalCustomizingConsoleTestFragmentConfigurations());
		allBundles.putAll(getAdditionalXentisTestFragmentConfigurations());

		return allBundles;
	}

	private Map<String, ProjectConfiguration> getAdditionalXnifeTestFragmentConfigurations() {
		Map<String, ProjectConfiguration> allBundles = new HashMap<>();
		ProjectConfiguration aConfiguration;

		aConfiguration = new ProjectConfiguration(null);
		aConfiguration.additionalBundles.addAll(someAdditionalTestRuntimeBundles);
		aConfiguration.additionalBundles.add("jcl.over.slf4j");
		aConfiguration.additionalBundles.add("org.springframework.expression");
		allBundles.put("com.xnife.cache.test", aConfiguration);

		aConfiguration = new ProjectConfiguration(null);
		aConfiguration.additionalBundles.addAll(someAdditionalTestRuntimeBundles);
		allBundles.put("com.xnife.context.application.test", aConfiguration);

		aConfiguration = new ProjectConfiguration(null);
		aConfiguration.additionalBundles.addAll(someAdditionalTestRuntimeBundles);
		allBundles.put("com.xnife.context.persistence.test", aConfiguration);

		aConfiguration = new ProjectConfiguration(null);
		aConfiguration.additionalBundles.addAll(someAdditionalTestRuntimeBundles);
		aConfiguration.additionalPackageDependencies.add("org.mockito.stubbing");
		allBundles.put("com.xnife.context.service.impl.test", aConfiguration);

		aConfiguration = new ProjectConfiguration(null);
		aConfiguration.additionalPackageDependencies.add("org.hamcrest;core=split");
		aConfiguration.additionalBundles.addAll(someAdditionalTestRuntimeBundles);
		allBundles.put("com.xnife.core.test", aConfiguration);

		aConfiguration = new ProjectConfiguration(null);
		aConfiguration.additionalBundles.addAll(someAdditionalTestRuntimeBundles);
		allBundles.put("com.xnife.domain.test", aConfiguration);

		aConfiguration = new ProjectConfiguration(null);
		aConfiguration.additionalPackageDependencies.add("org.hamcrest;core=split");
		aConfiguration.additionalBundles.addAll(someAdditionalTestRuntimeBundles);
		allBundles.put("com.xnife.encryption.service.xentis.test", aConfiguration);

		aConfiguration = new ProjectConfiguration(null);
		aConfiguration.additionalBundles.addAll(someAdditionalTestRuntimeBundles);
		allBundles.put("com.xnife.environment.test", aConfiguration);

		aConfiguration = new ProjectConfiguration(null);
		aConfiguration.additionalBundles.addAll(someAdditionalTestRuntimeBundles);
		allBundles.put("com.xnife.exception.test", aConfiguration);

		aConfiguration = new ProjectConfiguration(null);
		aConfiguration.additionalPackageDependencies.add("org.hamcrest;core=split");
		aConfiguration.additionalBundles.addAll(someAdditionalTestRuntimeBundles);
		allBundles.put("com.xnife.finance.test", aConfiguration);

		aConfiguration = new ProjectConfiguration(null);
		aConfiguration.additionalPackageDependencies.add("org.springframework.util");
		aConfiguration.additionalPackageDependencies.add("org.mockito.stubbing");
		aConfiguration.additionalBundles.addAll(someAdditionalTestRuntimeBundles);
		aConfiguration.additionalBundles.add("jcl.over.slf4j");
		allBundles.put("com.xnife.http.server.test", aConfiguration);

		aConfiguration = new ProjectConfiguration(null);
		aConfiguration.additionalBundles.addAll(someAdditionalTestRuntimeBundles);
		allBundles.put("com.xnife.localization.test", aConfiguration);

		aConfiguration = new ProjectConfiguration(null);
		aConfiguration.additionalPackageDependencies.add("org.hamcrest;core=split");
		aConfiguration.additionalBundles.addAll(someAdditionalTestRuntimeBundles);
		allBundles.put("com.xnife.logging.slf4j.logback.test", aConfiguration);

		aConfiguration = new ProjectConfiguration(null);
		aConfiguration.additionalBundles.addAll(someAdditionalTestRuntimeBundles);
		allBundles.put("com.xnife.metadata.test", aConfiguration);

		aConfiguration = new ProjectConfiguration(null);
		aConfiguration.additionalPackageDependencies.add("org.hamcrest;core=split");
		aConfiguration.additionalBundles.addAll(someAdditionalTestRuntimeBundles);
		allBundles.put("com.xnife.microbenchmark.test", aConfiguration);

		aConfiguration = new ProjectConfiguration(null);
		aConfiguration.additionalBundles.addAll(someAdditionalTestRuntimeBundles);
		allBundles.put("com.xnife.presentation.test", aConfiguration);

		aConfiguration = new ProjectConfiguration(null);
		aConfiguration.additionalBundles.addAll(someAdditionalTestRuntimeBundles);
		aConfiguration.additionalBundles.add("jcl.over.slf4j");
		allBundles.put("com.xnife.remoting.osgi.test", aConfiguration);

		aConfiguration = new ProjectConfiguration(null);
		aConfiguration.additionalBundles.addAll(someAdditionalTestRuntimeBundles);
		allBundles.put("com.xnife.resource.test", aConfiguration);

		aConfiguration = new ProjectConfiguration(null);
		aConfiguration.additionalBundles.addAll(someAdditionalTestRuntimeBundles);
		allBundles.put("com.xnife.security.test", aConfiguration);

		aConfiguration = new ProjectConfiguration(null);
		aConfiguration.additionalPackageDependencies.add("org.hamcrest;core=split");
		aConfiguration.additionalBundles.addAll(someAdditionalTestRuntimeBundles);
		allBundles.put("com.xnife.spring.osgi.test", aConfiguration);

		aConfiguration = new ProjectConfiguration(null);
		aConfiguration.additionalPackageDependencies.add("org.springframework.expression");
		aConfiguration.additionalBundles.addAll(someAdditionalTestRuntimeBundles);
		allBundles.put("com.xnife.spring.test", aConfiguration);

		aConfiguration = new ProjectConfiguration(null);
		aConfiguration.additionalBundles.addAll(someAdditionalTestRuntimeBundles);
		allBundles.put("com.xnife.testing.junit.test", aConfiguration);

		aConfiguration = new ProjectConfiguration(null);
		aConfiguration.additionalPackageDependencies.add("org.hamcrest;core=split");
		aConfiguration.additionalBundles.addAll(someAdditionalTestRuntimeBundles);
		allBundles.put("com.xnife.tools.rcp.configurator.test", aConfiguration);

		return allBundles;
	}

	private Map<String, ProjectConfiguration> getAdditionalPlatformTestFragmentConfigurations() {
		Map<String, ProjectConfiguration> allBundles = new HashMap<>();
		ProjectConfiguration aConfiguration;

		aConfiguration = new ProjectConfiguration(null);
		aConfiguration.additionalBundles.addAll(someAdditionalTestRuntimeBundles);
		allBundles.put("com.compxc.common.domain.test", aConfiguration);

		aConfiguration = new ProjectConfiguration(null);
		aConfiguration.additionalBundles.addAll(someAdditionalTestRuntimeBundles);
		allBundles.put("com.compxc.common.persistence.test", aConfiguration);

		aConfiguration = new ProjectConfiguration(null);
		aConfiguration.additionalBundles.addAll(someAdditionalTestRuntimeBundles);
		allBundles.put("com.compxc.platform.common.test", aConfiguration);

		aConfiguration = new ProjectConfiguration(null);
		aConfiguration.additionalPackageDependencies.add("org.hamcrest;core=split");
		aConfiguration.additionalBundles.addAll(someAdditionalTestRuntimeBundles);
		allBundles.put("com.compxc.workbench.presentation.test", aConfiguration);

		aConfiguration = new ProjectConfiguration(null);
		aConfiguration.additionalBundles.addAll(someAdditionalTestRuntimeBundles);
		allBundles.put("com.profidata.xc.calendar.service.impl.test", aConfiguration);

		aConfiguration = new ProjectConfiguration(null);
		aConfiguration.additionalBundles.addAll(someAdditionalTestRuntimeBundles);
		allBundles.put("com.profidata.xc.codevalue.service.impl.test", aConfiguration);

		aConfiguration = new ProjectConfiguration(null);
		aConfiguration.additionalBundles.addAll(someAdditionalTestRuntimeBundles);
		allBundles.put("com.profidata.xc.common.logger.test", aConfiguration);

		aConfiguration = new ProjectConfiguration(null);
		aConfiguration.additionalPackageDependencies.add("org.hamcrest;core=split");
		aConfiguration.additionalBundles.addAll(someAdditionalTestRuntimeBundles);
		allBundles.put("com.profidata.xc.data.service.domain.test", aConfiguration);

		aConfiguration = new ProjectConfiguration(null);
		aConfiguration.additionalPackageDependencies.add("org.hamcrest;core=split");
		aConfiguration.additionalBundles.addAll(someAdditionalTestRuntimeBundles);
		allBundles.put("com.profidata.xc.metadata.service.impl.test", aConfiguration);

		aConfiguration = new ProjectConfiguration(null);
		aConfiguration.additionalBundles.addAll(someAdditionalTestRuntimeBundles);
		allBundles.put("com.profidata.xc.objectquery.test", aConfiguration);

		return allBundles;
	}

	private Map<String, ProjectConfiguration> getAdditionalRiskTestFragmentConfigurations() {
		Map<String, ProjectConfiguration> allBundles = new HashMap<>();
		ProjectConfiguration aConfiguration;

		aConfiguration = new ProjectConfiguration(null);
		aConfiguration.additionalPackageDependencies.add("com.profidata.xentis.domain.unified");
		aConfiguration.additionalPackageDependencies.add("com.profidata.xentis.domain.unified.impl.instrument");
		aConfiguration.additionalPackageDependencies.add("com.xnife.domain");
		aConfiguration.additionalBundles.addAll(someAdditionalTestRuntimeBundles);
		aConfiguration.additionalBundles.add("com.sun.istack.commons-runtime");
		allBundles.put("com.profidata.risk.commons.test", aConfiguration);

		aConfiguration = new ProjectConfiguration(null);
		aConfiguration.additionalBundles.addAll(someAdditionalTestRuntimeBundles);
		allBundles.put("com.profidata.xc.risk.domain.test", aConfiguration);

		aConfiguration = new ProjectConfiguration(null);
		aConfiguration.additionalPackageDependencies.add("org.apache.poi.ooxml");
		aConfiguration.additionalBundles.addAll(someAdditionalTestRuntimeBundles);
		aConfiguration.additionalBundles.add("org.apache.commons.collections4");
		aConfiguration.additionalBundles.add("org.apache.commons.compress");
		allBundles.put("com.profidata.xc.risk.excel.test", aConfiguration);

		aConfiguration = new ProjectConfiguration(null);
		aConfiguration.additionalBundles.addAll(someAdditionalTestRuntimeBundles);
		allBundles.put("com.profidata.xc.risk.presentation.test", aConfiguration);

		aConfiguration = new ProjectConfiguration(null);
		aConfiguration.additionalPackageDependencies.add("org.springframework.context");
		aConfiguration.additionalPackageDependencies.add("org.reactivestreams");
		aConfiguration.additionalPackageDependencies.add("org.mockito.stubbing");
		aConfiguration.additionalBundles.addAll(someAdditionalTestRuntimeBundles);
		aConfiguration.additionalBundles.add("com.sun.istack.commons-runtime");
		allBundles.put("com.profidata.xc.risk.service.impl.test", aConfiguration);

		return allBundles;
	}

	private Map<String, ProjectConfiguration> getAdditionalMiddleOfficeTestFragmentConfigurations() {
		Map<String, ProjectConfiguration> allBundles = new HashMap<>();
		ProjectConfiguration aConfiguration;

		aConfiguration = new ProjectConfiguration(null);
		aConfiguration.additionalBundles.addAll(someAdditionalTestRuntimeBundles);
		aConfiguration.additionalBundles.add("com.sun.istack.commons-runtime");
		allBundles.put("com.compxc.order.domain.test", aConfiguration);

		aConfiguration = new ProjectConfiguration(null);
		aConfiguration.additionalPackageDependencies.add("org.powermock.modules.junit4.common.internal.impl");
		aConfiguration.additionalPackageDependencies.add("org.powermock.api.support.membermodification");
		aConfiguration.additionalPackageDependencies.add("org.mockito.stubbing");
		aConfiguration.additionalPackageDependencies.add("net.bytebuddy.dynamic.loading");
		aConfiguration.additionalPackageDependencies.add("com.xnife.core.beanutil");
		aConfiguration.additionalBundles.addAll(someAdditionalTestRuntimeBundles);
		allBundles.put("com.compxc.order.presentation.test", aConfiguration);

		return allBundles;
	}

	private Map<String, ProjectConfiguration> getAdditionalFrontOfficeTestFragmentConfigurations() {
		Map<String, ProjectConfiguration> allBundles = new HashMap<>();
		ProjectConfiguration aConfiguration;

		aConfiguration = new ProjectConfiguration(null);
		aConfiguration.additionalPackageDependencies.add("javax.annotation");
		aConfiguration.additionalBundles.addAll(someAdditionalTestRuntimeBundles);
		allBundles.put("com.compxc.frontoffice.common.test", aConfiguration);

		aConfiguration = new ProjectConfiguration(null);
		aConfiguration.additionalPackageDependencies.add("org.powermock.modules.junit4.common.internal.impl");
		aConfiguration.additionalPackageDependencies.add("com.compxc.common.persistence.entity.db");
		aConfiguration.additionalPackageDependencies.add("org.powermock.api.support.membermodification");
		aConfiguration.additionalBundles.addAll(someAdditionalTestRuntimeBundles);
		allBundles.put("com.compxc.frontoffice.excel.windows.test", aConfiguration);

		aConfiguration = new ProjectConfiguration(null);
		aConfiguration.additionalPackageDependencies.add("org.hamcrest;core=split");
		aConfiguration.additionalPackageDependencies.add("org.mockito.stubbing");
		aConfiguration.additionalPackageDependencies.add("org.mockito.invocation");
		aConfiguration.additionalPackageDependencies.add("org.powermock.modules.junit4.common.internal.impl");
		aConfiguration.additionalPackageDependencies.add("org.powermock.api.support.membermodification");
		aConfiguration.additionalBundles.addAll(someAdditionalTestRuntimeBundles);
		aConfiguration.additionalBundles.add("org.powermock.core");
		aConfiguration.additionalBundles.add("org.apache.commons.collections4");
		aConfiguration.additionalBundles.add("com.zaxxer.sparsebitset");
		allBundles.put("com.compxc.frontoffice.presentation.test", aConfiguration);

		aConfiguration = new ProjectConfiguration(null);
		aConfiguration.additionalPackageDependencies.add("org.springframework.remoting.support");
		aConfiguration.additionalPackageDependencies.add("org.mockito.stubbing");
		aConfiguration.additionalPackageDependencies.add("org.springframework.expression");
		aConfiguration.additionalBundles.addAll(someAdditionalTestRuntimeBundles);
		allBundles.put("com.profidata.xc.frontoffice.server.metadata.test", aConfiguration);

		aConfiguration = new ProjectConfiguration(null);
		aConfiguration.additionalPackageDependencies.add("org.hamcrest;core=split");
		aConfiguration.additionalPackageDependencies.add("org.springframework.remoting.support");
		aConfiguration.additionalPackageDependencies.add("org.mockito.stubbing");
		aConfiguration.additionalBundles.addAll(someAdditionalTestRuntimeBundles);
		allBundles.put("com.profidata.xc.frontoffice.service.impl.test", aConfiguration);

		aConfiguration = new ProjectConfiguration(null);
		aConfiguration.additionalPackageDependencies.add("org.hamcrest;core=split");
		aConfiguration.additionalBundles.addAll(someAdditionalTestRuntimeBundles);
		allBundles.put("com.profidata.xc.frontoffice.test", aConfiguration);

		aConfiguration = new ProjectConfiguration(null);
		aConfiguration.additionalPackageDependencies.add("org.powermock.reflect");
		aConfiguration.additionalPackageDependencies.add("org.powermock.modules.junit4.common.internal.impl");
		aConfiguration.additionalPackageDependencies.add("org.powermock.api.support.membermodification");
		aConfiguration.additionalPackageDependencies.add("com.compxc.platform.common.metadata");
		aConfiguration.additionalBundles.addAll(someAdditionalTestRuntimeBundles);
		aConfiguration.additionalBundles.add("org.glassfish.hk2");
		allBundles.put("com.profidata.xc.rest.analysis.service.impl.test", aConfiguration);

		aConfiguration = new ProjectConfiguration(null);
		aConfiguration.additionalPackageDependencies.add("com.profidata.xc.rest.common.service");
		aConfiguration.additionalPackageDependencies.add("org.powermock.api.support.membermodification");
		aConfiguration.additionalPackageDependencies.add("org.powermock.core.spi");
		aConfiguration.additionalBundles.addAll(someAdditionalTestRuntimeBundles);
		aConfiguration.additionalBundles.add("com.google.guava");
		aConfiguration.additionalBundles.add("com.google.guava.failureaccess");
		aConfiguration.additionalBundles.add("org.glassfish.jersey");
		aConfiguration.additionalBundles.add("org.glassfish.hk2");
		aConfiguration.additionalBundles.add("javax.inject");
		allBundles.put("com.profidata.xc.rest.common.service.impl.test", aConfiguration);

		aConfiguration = new ProjectConfiguration(null);
		aConfiguration.additionalBundles.addAll(someAdditionalTestRuntimeBundles);
		aConfiguration.additionalBundles.add("org.glassfish.hk2");
		allBundles.put("com.profidata.xc.rest.investmentcompliance.service.impl.test", aConfiguration);

		return allBundles;
	}

	private Map<? extends String, ? extends ProjectConfiguration> getAdditionalCustomizingConsoleTestFragmentConfigurations() {
		Map<String, ProjectConfiguration> allBundles = new HashMap<>();
		ProjectConfiguration aConfiguration;

		aConfiguration = new ProjectConfiguration(null);
		aConfiguration.additionalBundles.addAll(someAdditionalTestRuntimeBundles);
		allBundles.put("com.profidata.xc.customizingconsole.presentation.test", aConfiguration);

		return allBundles;
	}

	private Map<String, ProjectConfiguration> getAdditionalXentisTestFragmentConfigurations() {
		Map<String, ProjectConfiguration> allBundles = new HashMap<>();
		ProjectConfiguration aConfiguration;

		aConfiguration = new ProjectConfiguration(null);
		aConfiguration.additionalPackageDependencies.add("org.hamcrest;core=split");
		aConfiguration.additionalBundles.addAll(someAdditionalTestRuntimeBundles);
		allBundles.put("com.profidata.reporting.common.test", aConfiguration);

		aConfiguration = new ProjectConfiguration(null);
		aConfiguration.additionalBundles.addAll(someAdditionalTestRuntimeBundles);
		aConfiguration.additionalBundles.add("com.sun.istack.commons-runtime");
		allBundles.put("com.profidata.xentis.bodomain.test", aConfiguration);

		aConfiguration = new ProjectConfiguration(null);
		aConfiguration.additionalPackageDependencies.add("org.hamcrest;core=split");
		aConfiguration.additionalPackageDependencies.add("javax.annotation");
		aConfiguration.additionalBundles.addAll(someAdditionalTestRuntimeBundles);
		aConfiguration.additionalBundles.add("com.sun.istack.commons-runtime");
		allBundles.put("com.profidata.xentis.domain.unified.test", aConfiguration);

		aConfiguration = new ProjectConfiguration(null);
		aConfiguration.additionalPackageDependencies.add("org.hamcrest;core=split");
		aConfiguration.additionalPackageDependencies.add("org.springframework.context");
		aConfiguration.additionalPackageDependencies.add("org.springframework.aop");
		aConfiguration.additionalPackageDependencies.add("org.springframework.expression");
		aConfiguration.additionalBundles.addAll(someAdditionalTestRuntimeBundles);
		aConfiguration.additionalBundles.add("jcl.over.slf4j");
		allBundles.put("com.profidata.xentis.env.client.test", aConfiguration);

		aConfiguration = new ProjectConfiguration(null);
		aConfiguration.additionalPackageDependencies.add("org.hamcrest;core=split");
		aConfiguration.additionalPackageDependencies.add("org.springframework.aop");
		aConfiguration.additionalPackageDependencies.add("org.springframework.expression");
		aConfiguration.additionalPackageDependencies.add("org.mockito.stubbing");
		aConfiguration.additionalBundles.addAll(someAdditionalTestRuntimeBundles);
		aConfiguration.additionalBundles.add("jcl.over.slf4j");
		allBundles.put("com.profidata.xentis.env.shared.test", aConfiguration);

		aConfiguration = new ProjectConfiguration(null);
		aConfiguration.additionalPackageDependencies.add("org.hamcrest;core=split");
		aConfiguration.additionalBundles.addAll(someAdditionalTestRuntimeBundles);
		aConfiguration.additionalBundles.add("net.lingala.zip4j");
		allBundles.put("com.profidata.xentis.etl.commons.test", aConfiguration);

		aConfiguration = new ProjectConfiguration(null);
		aConfiguration.additionalPackageDependencies.add("org.hamcrest;core=split");
		aConfiguration.additionalBundles.addAll(someAdditionalTestRuntimeBundles);
		allBundles.put("com.profidata.xentis.fix.commons.test", aConfiguration);

		aConfiguration = new ProjectConfiguration(null);
		aConfiguration.additionalPackageDependencies.add("org.hamcrest;library=split");
		aConfiguration.additionalPackageDependencies.add("com.profidatagroup.xentis.ui.base.lookandfeel");
		aConfiguration.additionalPackageDependencies.add("org.powermock.modules.junit4.common.internal.impl");
		aConfiguration.additionalPackageDependencies.add("org.powermock.api.support.membermodification");
		aConfiguration.additionalPackageDependencies.add("org.powermock.reflect");
		aConfiguration.additionalPackageDependencies.add("org.mockito");
		aConfiguration.additionalPackageDependencies.add("org.mockito.stubbing");
		aConfiguration.additionalPackageDependencies.add("org.apache.commons.text.lookup");
		aConfiguration.additionalBundles.addAll(someAdditionalTestRuntimeBundles);
		allBundles.put("com.profidata.xentis.javamis.client.test", aConfiguration);

		aConfiguration = new ProjectConfiguration(null);
		aConfiguration.additionalPackageDependencies.add("org.springframework.beans.factory.annotation");
		aConfiguration.additionalPackageDependencies.add("org.mockito.stubbing");
		aConfiguration.additionalBundles.addAll(someAdditionalTestRuntimeBundles);
		aConfiguration.additionalBundles.add("com.google.guava");
		allBundles.put("com.profidata.xentis.javamis.shared.test", aConfiguration);

		aConfiguration = new ProjectConfiguration(null);
		aConfiguration.additionalPackageDependencies.add("org.springframework.context");
		aConfiguration.additionalPackageDependencies.add("javax.management.j2ee.statistics");
		aConfiguration.additionalBundles.addAll(someAdditionalTestRuntimeBundles);
		allBundles.put("com.profidata.xentis.jms.shared.test", aConfiguration);

		aConfiguration = new ProjectConfiguration(null);
		aConfiguration.additionalPackageDependencies.add("org.hamcrest;core=split");
		aConfiguration.additionalBundles.addAll(someAdditionalTestRuntimeBundles);
		allBundles.put("com.profidata.xentis.jni.common.test", aConfiguration);

		aConfiguration = new ProjectConfiguration(null);
		aConfiguration.additionalBundles.addAll(someAdditionalTestRuntimeBundles);
		allBundles.put("com.profidata.xentis.test", aConfiguration);

		aConfiguration = new ProjectConfiguration(null);
		aConfiguration.additionalPackageDependencies.add("org.hamcrest;core=split");
		aConfiguration.additionalBundles.addAll(someAdditionalTestRuntimeBundles);
		allBundles.put("com.profidata.xentis.ui.base.test", aConfiguration);

		aConfiguration = new ProjectConfiguration(null);
		aConfiguration.additionalPackageDependencies.add("org.hamcrest;core=split");
		aConfiguration.additionalBundles.addAll(someAdditionalTestRuntimeBundles);
		allBundles.put("com.profidata.xentis.user.shared.test", aConfiguration);

		return allBundles;
	}

}
