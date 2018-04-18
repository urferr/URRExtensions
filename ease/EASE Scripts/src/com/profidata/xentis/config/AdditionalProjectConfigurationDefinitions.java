package com.profidata.xentis.config;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AdditionalProjectConfigurationDefinitions {
	public final Map<String, ProjectConfiguration> projectConfigurations = new HashMap<>();

	public static class ProjectConfiguration {
		public final String encoding;
		public final Set<String> additionalPackageDependencies = new HashSet<>();
		public final Set<String> additionalProjectDependencies = new HashSet<>();
		public final Set<ClasspathEntry> additionalClasspathEntries = new HashSet<>();

		public ProjectConfiguration(String theEncoding) {
			encoding = theEncoding;
		}
	}

	public static class ClasspathEntry {
		public enum ClasspathEntryType {
			Library,
			Project,
			Container
		}

		public final ClasspathEntryType type;
		public final String path;
		public final boolean exported;
		public final Set<AccessRule> accessRules = new HashSet<>();

		public ClasspathEntry(ClasspathEntryType theType, String thePath) {
			this(theType, thePath, false);
		}

		public ClasspathEntry(ClasspathEntryType theType, String thePath, boolean theExported) {
			type = theType;
			path = thePath;
			exported = theExported;
		}
	}

	public static class AccessRule {
		public final String pattern;
		public final int kind;

		public AccessRule(String thePattern, int theKind) {
			pattern = thePattern;
			kind = theKind;
		}
	}
}
