package com.profidata.xentis.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class ProjectDependencyConfiguration {
	public static final Map<String, Set<String>> additionalProjectDependencies;

	static {
		Set<String> someSourceProjects;

		additionalProjectDependencies = new HashMap<>();

		someSourceProjects = new TreeSet<>();
		someSourceProjects.add("com.profidata.xentis.session");
		someSourceProjects.add("com.profidata.xentis.trx");
		additionalProjectDependencies.put("com.profidata.xentis.javamis", someSourceProjects);
	}

}
