package com.profidata.xentis.config;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ProjectDependencyConfiguration {
	public static final Map<String, Set<String>> additionalProjectDependencies;

	static {
		Set<String> someSourceProjects;

		additionalProjectDependencies = new HashMap<>();

		someSourceProjects = new HashSet<>();
		someSourceProjects.add("com.profidata.xentis.session");
		additionalProjectDependencies.put("com.profidata.xentis.javamis", someSourceProjects);
	}

}
