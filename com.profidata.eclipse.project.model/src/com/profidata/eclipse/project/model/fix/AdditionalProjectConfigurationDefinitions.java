package com.profidata.eclipse.project.model.fix;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.profidata.eclipse.project.model.Activator;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;

public class AdditionalProjectConfigurationDefinitions {
	public final Map<String, ProjectConfiguration> projectConfigurations = new HashMap<>();

	public static class ProjectConfiguration {
		public final String encoding;
		public final Set<String> additionalPackageDependencies = new HashSet<>();
		public final Set<String> additionalProjectDependencies = new HashSet<>();
		public final Set<ClasspathEntry> additionalClasspathEntries = new HashSet<>();

		public ProjectConfiguration(String theEncoding) {
			this.encoding = theEncoding;
		}
	}

	public static class ClasspathEntry {
		public enum ClasspathEntryType {
			Library,
			Project,
			Container
		}

		final ClasspathEntryType type;
		final String path;
		public final boolean exported;
		public final Set<AccessRule> accessRules = new HashSet<>();

		public ClasspathEntry(ClasspathEntryType theType, String thePath) {
			this(theType, thePath, false);
		}

		public ClasspathEntry(ClasspathEntryType theType, String thePath, boolean theExported) {
			this.type = theType;
			this.path = thePath;
			this.exported = theExported;
		}
	}

	public static class AccessRule {
		public final String pattern;
		public final int kind;

		public AccessRule(String thePattern, int theKind) {
			this.pattern = thePattern;
			this.kind = theKind;
		}
	}

	public static ProjectConfiguration find(String theProjectName) {
		AdditionalProjectConfigurationDefinitions aConfiguration = null;
		IPath aAdditionalProjectConfigurationPath = ResourcesPlugin.getWorkspace().getRoot().getLocation().append(System.getProperty("additional.project.configuration.path", "URRExtensions/PDE-Targets & Launcher"))
				.append("AdditionProjectConfiguration.json");

		try (Reader aReader = new FileReader(aAdditionalProjectConfigurationPath.toOSString())) {
			aConfiguration = new Gson().fromJson(aReader, AdditionalProjectConfigurationDefinitions.class);

			if (aConfiguration != null && aConfiguration.projectConfigurations.containsKey(theProjectName)) {
				return aConfiguration.projectConfigurations.get(theProjectName);
			}
		}
		catch (JsonIOException | IOException cause) {
		    Activator.error("could not read json file: " + aAdditionalProjectConfigurationPath.toOSString());
		}

		return new ProjectConfiguration(null);
	}

}
