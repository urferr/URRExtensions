package com.profidata.xentis.scripts;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IAccessRule;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.profidata.eclipse.project.model.fix.AdditionalProjectConfigurationDefinitions;
import com.profidata.eclipse.project.model.fix.AdditionalProjectConfigurationDefinitions.AccessRule;
import com.profidata.eclipse.project.model.fix.AdditionalProjectConfigurationDefinitions.ClasspathEntry;
import com.profidata.eclipse.project.model.fix.AdditionalProjectConfigurationDefinitions.ClasspathEntry.ClasspathEntryType;
import com.profidata.eclipse.project.model.fix.AdditionalProjectConfigurationDefinitions.ProjectConfiguration;

public class SaveConfigurationAsJson {
	private static PrintStream output;
	private static PrintStream error;

	public static void initialize(InputStream theInput, PrintStream theOutput, PrintStream theError) {
		SaveConfigurationAsJson.output = theOutput;
		SaveConfigurationAsJson.error = theError;
	}

	public static void main(String[] args) throws Exception {
		new SaveConfigurationAsJson().execute();
	}

	private void execute() {
		AdditionalProjectConfigurationDefinitions aConfiguration = createConfig("JavaSE-1.8");
		IPath aAdditionalProjectConfigurationPath = ResourcesPlugin.getWorkspace().getRoot().getLocation().append("URRExtensions/ease/EASE Scripts").append("AdditionProjectConfiguration.json");
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
			aConfiguration = gson.fromJson(aReader, AdditionalProjectConfigurationDefinitions.class);
		}
		catch (JsonIOException | IOException cause) {
			output.println("could not read json file: " + aAdditionalProjectConfigurationPath.toOSString());
		}

		// 2. Java object to JSON, and assign to a String
		String jsonInString = gson.toJson(aConfiguration);

		output.println(jsonInString);
	}

	private AdditionalProjectConfigurationDefinitions createConfig(String theExecutionEnvironment) {
		AdditionalProjectConfigurationDefinitions aProjectConfiguration = new AdditionalProjectConfigurationDefinitions(theExecutionEnvironment);

		aProjectConfiguration.projectConfigurations.putAll(getAdditionalBundleConfigurations());
		aProjectConfiguration.projectConfigurations.putAll(getAdditionalTestFragmentConfigurations());

		return aProjectConfiguration;
	}

	private Map<String, ProjectConfiguration> getAdditionalBundleConfigurations() {
		Map<String, ProjectConfiguration> allBundles = new HashMap<>();
		ProjectConfiguration aConfiguration;

		aConfiguration = new ProjectConfiguration("UTF-8");
		aConfiguration.additionalProjectDependencies.add("com.profidata.xentis.session");
		aConfiguration.additionalClasspathEntries.add(new ClasspathEntry(ClasspathEntryType.Library, "provided/atdl4j.jar"));
		aConfiguration.additionalClasspathEntries.add(new ClasspathEntry(ClasspathEntryType.Library, "provided/com.profidata.xentis.env.server.jar", true));
		aConfiguration.additionalClasspathEntries.add(new ClasspathEntry(ClasspathEntryType.Library, "provided/com.profidata.xentis.jni.jar", true));
		aConfiguration.additionalClasspathEntries.add(new ClasspathEntry(ClasspathEntryType.Library, "provided/com.profidata.xentis.ratex.jar", true));
		aConfiguration.additionalClasspathEntries.add(new ClasspathEntry(ClasspathEntryType.Library, "provided/com.profidata.xentis.sn.jar", true));
		aConfiguration.additionalClasspathEntries.add(new ClasspathEntry(ClasspathEntryType.Library, "provided/com.profidatagroup.util.keymigration.model.jar"));
		aConfiguration.additionalClasspathEntries.add(new ClasspathEntry(ClasspathEntryType.Library, "provided/foxtrot.jar"));
		aConfiguration.additionalClasspathEntries.add(new ClasspathEntry(ClasspathEntryType.Library, "provided/hawtbuf.jar"));
		aConfiguration.additionalClasspathEntries.add(new ClasspathEntry(ClasspathEntryType.Library, "provided/jaxrpc.jar"));
		aConfiguration.additionalClasspathEntries.add(new ClasspathEntry(ClasspathEntryType.Library, "provided/org.jzy3d.jar"));
		aConfiguration.additionalClasspathEntries.add(new ClasspathEntry(ClasspathEntryType.Library, "provided/ratex.jar"));
		aConfiguration.additionalClasspathEntries.add(new ClasspathEntry(ClasspathEntryType.Library, "provided/jxbrowser.jar"));
		aConfiguration.additionalClasspathEntries.add(new ClasspathEntry(ClasspathEntryType.Project, "com.profidata.xentis.session"));

		ClasspathEntry aClasspathEntry = new ClasspathEntry(ClasspathEntryType.Container, "org.eclipse.jdt.launching.JRE_CONTAINER/org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType/JavaSE-1.8");
		aClasspathEntry.accessRules.add(new AccessRule("com/sun/java/swing/plaf/windows/*", IAccessRule.K_ACCESSIBLE));
		aClasspathEntry.accessRules.add(new AccessRule("sun/awt/shell/*", IAccessRule.K_ACCESSIBLE));
		aConfiguration.additionalClasspathEntries.add(aClasspathEntry);
		allBundles.put("com.profidata.xentis.javamis", aConfiguration);

		aConfiguration = new ProjectConfiguration("UTF-8");
		allBundles.put("com.profidata.xentis.javamisxc", aConfiguration);

		aConfiguration = new ProjectConfiguration(null);
		aConfiguration.additionalClasspathEntries.add(new ClasspathEntry(ClasspathEntryType.Library, "antlr-runtime-3.2.jar"));
		allBundles.put("com.xnife.objectquery.plan.parser", aConfiguration);

		return allBundles;
	}

	private Map<String, ProjectConfiguration> getAdditionalTestFragmentConfigurations() {
		Map<String, ProjectConfiguration> allBundles = new HashMap<>();
		ProjectConfiguration aConfiguration;

		aConfiguration = new ProjectConfiguration(null);
		aConfiguration.additionalPackageDependencies.add("com.profidata.xentis.domain.unified");
		aConfiguration.additionalPackageDependencies.add("com.profidata.xentis.domain.unified.impl.instrument");
		aConfiguration.additionalPackageDependencies.add("com.xnife.domain");
		allBundles.put("com.profidata.risk.commons.test", aConfiguration);

		aConfiguration = new ProjectConfiguration(null);
		aConfiguration.additionalPackageDependencies.add("org.objenesis");
		allBundles.put("com.profidata.xentis.bodomain.test", aConfiguration);

		aConfiguration = new ProjectConfiguration(null);
		aConfiguration.additionalPackageDependencies.add("org.objenesis");
		aConfiguration.additionalPackageDependencies.add("org.apache.commons.logging");
		aConfiguration.additionalPackageDependencies.add("org.springframework.aop");
		aConfiguration.additionalPackageDependencies.add("org.springframework.expression");
		allBundles.put("com.profidata.xentis.env.shared.test", aConfiguration);

		aConfiguration = new ProjectConfiguration(null);
		aConfiguration.additionalPackageDependencies.add("org.hamcrest;core=split");
		aConfiguration.additionalPackageDependencies.add("org.objenesis");
		allBundles.put("com.profidata.xentis.etl.commons.test", aConfiguration);

		aConfiguration = new ProjectConfiguration(null);
		aConfiguration.additionalPackageDependencies.add("org.hamcrest;core=split");
		allBundles.put("com.profidata.xentis.fix.commons.test", aConfiguration);

		aConfiguration = new ProjectConfiguration(null);
		aConfiguration.additionalPackageDependencies.add("org.hamcrest;library=split");
		aConfiguration.additionalPackageDependencies.add("org.objenesis");
		aConfiguration.additionalPackageDependencies.add("com.profidatagroup.xentis.ui.base.lookandfeel");
		aConfiguration.additionalPackageDependencies.add("org.powermock.modules.junit4.common.internal.impl");
		aConfiguration.additionalPackageDependencies.add("org.powermock.api.support.membermodification");
		aConfiguration.additionalPackageDependencies.add("org.powermock.reflect");
		allBundles.put("com.profidata.xentis.javamis.test", aConfiguration);

		aConfiguration = new ProjectConfiguration(null);
		aConfiguration.additionalPackageDependencies.add("org.springframework.context");
		aConfiguration.additionalPackageDependencies.add("org.objenesis");
		aConfiguration.additionalPackageDependencies.add("javax.management.j2ee.statistics");
		allBundles.put("com.profidata.xentis.jms.shared.test", aConfiguration);

		aConfiguration = new ProjectConfiguration(null);
		aConfiguration.additionalPackageDependencies.add("org.springframework.context");
		aConfiguration.additionalPackageDependencies.add("org.objenesis");
		aConfiguration.additionalPackageDependencies.add("javax.management.j2ee.statistics");
		allBundles.put("com.profidata.xentis.jms.shared.test", aConfiguration);

		aConfiguration = new ProjectConfiguration(null);
		aConfiguration.additionalPackageDependencies.add("org.hamcrest;core=split");
		allBundles.put("com.profidata.xentis.domain.unified.test", aConfiguration);

		aConfiguration = new ProjectConfiguration(null);
		aConfiguration.additionalPackageDependencies.add("org.springframework.expression");
		allBundles.put("com.xnife.spring.test", aConfiguration);

		aConfiguration = new ProjectConfiguration(null);
		aConfiguration.additionalPackageDependencies.add("org.springframework.context");
		allBundles.put("com.profidata.xentis.env.client.test", aConfiguration);

		aConfiguration = new ProjectConfiguration(null);
		aConfiguration.additionalPackageDependencies.add("org.hamcrest;core=split");
		allBundles.put("com.compxc.workbench.presentation.test", aConfiguration);

		aConfiguration = new ProjectConfiguration(null);
		aConfiguration.additionalPackageDependencies.add("org.hamcrest;core=split");
		allBundles.put("com.profidata.xc.data.service.domain.test", aConfiguration);

		aConfiguration = new ProjectConfiguration(null);
		aConfiguration.additionalPackageDependencies.add("org.hamcrest;core=split");
		allBundles.put("com.profidata.xc.metadata.service.impl.test", aConfiguration);

		aConfiguration = new ProjectConfiguration(null);
		aConfiguration.additionalPackageDependencies.add("org.powermock.modules.junit4.common.internal.impl");
		aConfiguration.additionalPackageDependencies.add("org.powermock.api.support.membermodification");
		aConfiguration.additionalPackageDependencies.add("org.mockito.stubbing");
		aConfiguration.additionalPackageDependencies.add("net.bytebuddy.dynamic.loading");
		aConfiguration.additionalPackageDependencies.add("org.objenesis");
		allBundles.put("com.compxc.order.presentation.test", aConfiguration);

		aConfiguration = new ProjectConfiguration(null);
		aConfiguration.additionalPackageDependencies.add("org.powermock.modules.junit4.common.internal.impl");
		aConfiguration.additionalPackageDependencies.add("com.compxc.common.persistence.entity.db");
		aConfiguration.additionalPackageDependencies.add("org.powermock.api.support.membermodification");
		allBundles.put("com.compxc.frontoffice.excel.windows.test", aConfiguration);

		aConfiguration = new ProjectConfiguration(null);
		aConfiguration.additionalPackageDependencies.add("javax.annotation");
		allBundles.put("com.compxc.frontoffice.common.test", aConfiguration);

		aConfiguration = new ProjectConfiguration(null);
		aConfiguration.additionalPackageDependencies.add("org.hamcrest;core=split");
		aConfiguration.additionalPackageDependencies.add("org.powermock.modules.junit4.common.internal.impl");
		aConfiguration.additionalPackageDependencies.add("org.powermock.api.support.membermodification");
		allBundles.put("com.compxc.frontoffice.presentation.test", aConfiguration);

		aConfiguration = new ProjectConfiguration(null);
		aConfiguration.additionalPackageDependencies.add("org.hamcrest;core=split");
		aConfiguration.additionalPackageDependencies.add("org.springframework.remoting.support");
		aConfiguration.additionalPackageDependencies.add("org.mockito.stubbing");
		allBundles.put("com.profidata.xc.frontoffice.service.impl.test", aConfiguration);

		aConfiguration = new ProjectConfiguration(null);
		aConfiguration.additionalPackageDependencies.add("org.hamcrest;core=split");
		allBundles.put("com.profidata.xc.frontoffice.test", aConfiguration);

		aConfiguration = new ProjectConfiguration(null);
		aConfiguration.additionalPackageDependencies.add("com.profidata.xc.rest.common.service");
		aConfiguration.additionalPackageDependencies.add("org.powermock.api.support.membermodification");
		allBundles.put("com.profidata.xc.rest.common.service.impl.test", aConfiguration);

		aConfiguration = new ProjectConfiguration(null);
		aConfiguration.additionalPackageDependencies.add("org.powermock.reflect");
		aConfiguration.additionalPackageDependencies.add("org.powermock.modules.junit4.common.internal.impl");
		aConfiguration.additionalPackageDependencies.add("org.powermock.api.support.membermodification");
		aConfiguration.additionalPackageDependencies.add("org.objenesis");
		allBundles.put("com.profidata.xc.rest.analysis.service.impl.test", aConfiguration);

		aConfiguration = new ProjectConfiguration(null);
		aConfiguration.additionalPackageDependencies.add("org.springframework.remoting.support");
		allBundles.put("com.profidata.xc.frontoffice.server.metadata.test", aConfiguration);

		aConfiguration = new ProjectConfiguration(null);
		aConfiguration.additionalPackageDependencies.add("org.springframework.context");
		aConfiguration.additionalPackageDependencies.add("org.objenesis");
		allBundles.put("com.profidata.xc.risk.service.impl.test", aConfiguration);

		return allBundles;
	}

}
