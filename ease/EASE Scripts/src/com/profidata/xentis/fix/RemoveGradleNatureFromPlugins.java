package com.profidata.xentis.fix;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import com.profidata.xentis.util.ProjectConstants;
import com.profidata.xentis.util.ProjectWrapper;

public class RemoveGradleNatureFromPlugins {
	private static final Map<String, Set<String>> additionalTestFragmentDependencies;
	private static final Map<String, Set<String>> ignoreTestFragmentDependencies;

	private static final Map<String, Set<String>> additionalBundleDependencies;

	static {
		Set<String> somePackages;

		additionalBundleDependencies = new HashMap<>();

		somePackages = new HashSet<>();
		somePackages.add("org.springframework.beans");
		somePackages.add("org.springframework.beans.factory");
		somePackages.add("org.springframework.core.io.support");
		additionalBundleDependencies.put("com.profidata.xentis.env.shared", somePackages);

		somePackages = new HashSet<>();
		somePackages.add("org.springframework.context");
		somePackages.add("org.springframework.jms.support");
		somePackages.add("com.profidata.xentis.jni.common");
		additionalBundleDependencies.put("com.profidata.xentis.jms.shared", somePackages);
	}

	static {
		Set<String> somePackages;

		additionalTestFragmentDependencies = new HashMap<>();
		ignoreTestFragmentDependencies = new HashMap<>();

		somePackages = new HashSet<>();
		somePackages.add("com.profidata.xentis.domain.unified");
		somePackages.add("com.xnife.domain");
		additionalTestFragmentDependencies.put("com.profidata.risk.commons.test", somePackages);

		somePackages = new HashSet<>();
		somePackages.add("org.objenesis");
		additionalTestFragmentDependencies.put("com.profidata.xentis.bodomain.test", somePackages);

		somePackages = new HashSet<>();
		somePackages.add("org.objenesis");
		somePackages.add("org.apache.commons.logging");
		somePackages.add("org.springframework.aop");
		somePackages.add("org.springframework.expression");
		additionalTestFragmentDependencies.put("com.profidata.xentis.env.shared.test", somePackages);

		somePackages = new HashSet<>();
		somePackages.add("org.objenesis");
		additionalTestFragmentDependencies.put("com.profidata.xentis.etl.commons.test", somePackages);

		somePackages = new HashSet<>();
		somePackages.add("org.objenesis");
		additionalTestFragmentDependencies.put("com.profidata.xentis.javamis.test", somePackages);
		somePackages = new HashSet<>();
		somePackages.add("com.profidata.etl.commons");
		somePackages.add("com.profidata.xentis.env.client");
		somePackages.add("com.profidata.xentis.env.server");
		somePackages.add("com.profidata.xentis.sn");
		somePackages.add("org.fusesource.hawtbuf");
		somePackages.add("com.profidatagroup.javamis.client.ui.mainframe.schnittstelle.editor"); // don't know were it gets from???
		ignoreTestFragmentDependencies.put("com.profidata.xentis.javamis.test", somePackages);

		somePackages = new HashSet<>();
		somePackages.add("com.profidatagroup.javamis.client.rmi.presentation.snRATEX.definition"); // don't know were it gets from???
		ignoreTestFragmentDependencies.put("com.profidata.xentis.javamis.integration", somePackages);

		somePackages = new HashSet<>();
		somePackages.add("org.objenesis");
		somePackages.add("javax.management.j2ee.statistics");
		additionalTestFragmentDependencies.put("com.profidata.xentis.jms.shared.test", somePackages);

		somePackages = new HashSet<>();
		somePackages.add("org.springframework.expression");
		additionalTestFragmentDependencies.put("com.xnife.spring.test", somePackages);
	}

	private final PrintStream output;
	private final PrintStream error;

	public static void run(PrintStream theOutput, PrintStream theError) {
		new RemoveGradleNatureFromPlugins(theOutput, theError).execute();
	}

	private RemoveGradleNatureFromPlugins(PrintStream theOutput, PrintStream theError) {
		super();
		output = theOutput;
		error = theError;
	}

	private void execute() {
		IWorkspace aWorkspace = ResourcesPlugin.getWorkspace();

		findProjectsWithPluginAndGradleNature(aWorkspace).stream()
				.forEach(theProject -> {
					output.println("Fix project with Plugin/Gradle nature: " + theProject.getName());
					ProjectWrapper aProjectWrapper = ProjectWrapper.of(theProject)
							.asJavaProject()
							.removeNature(ProjectConstants.GRADLE_NATURE_ID)
							.removeClasspathEntry(new Path(ProjectConstants.GRADLE_CLASSPATH_ID))
							.addPackageDependenciesToPluginManifest(() -> additionalBundleDependencies.get(theProject.getName()))
							.refresh();

					if (aProjectWrapper.hasError()) {
						error.println("Fix project '" + theProject.getName() + "' failed:\n-> " + aProjectWrapper.getErrorMessage());
					}
					else {
						migrateTestSourceFolderToTestFragmentProject(theProject, "test");
						migrateTestSourceFolderToTestFragmentProject(theProject, "integration");
						migrateTestSourceFolderToTestFragmentProject(theProject, "manual");
					}
				});

	}

	private Collection<IProject> findProjectsWithPluginAndGradleNature(IWorkspace theWorkspace) {
		return Arrays.stream(theWorkspace.getRoot().getProjects())
				.filter(theProject -> hasNature(theProject, ProjectConstants.PLUGIN_NATURE_ID) && hasNature(theProject, ProjectConstants.GRADLE_NATURE_ID))
				.collect(Collectors.toSet());
	}

	private void migrateTestSourceFolderToTestFragmentProject(IProject theProject, String theTestType) {
		IJavaProject aJavaProject = JavaCore.create(theProject);

		try {
			IClasspathEntry[] allClasspathEntries = aJavaProject.getRawClasspath();
			List<IClasspathEntry> allTestSourceClasspathEntries;

			// first we check for unit tests
			allTestSourceClasspathEntries = Arrays.stream(allClasspathEntries)
					.filter(theEntry -> theEntry.getContentKind() == IPackageFragmentRoot.K_SOURCE && theEntry.getEntryKind() == IClasspathEntry.CPE_SOURCE)
					.filter(
							theEntry -> theEntry.getPath().removeFirstSegments(1).segment(0).equals(theTestType)
									|| (theEntry.getPath().removeFirstSegments(1).segmentCount() > 1 && theEntry.getPath().removeFirstSegments(1).segment(0).equals("src")
											&& theEntry.getPath().removeFirstSegments(1).segment(1).equals(theTestType)))
					.collect(Collectors.toList());

			if (!allTestSourceClasspathEntries.isEmpty()) {
				List<IClasspathEntry> allChangedClasspathEntries = new ArrayList<>(Arrays.asList(allClasspathEntries));

				allChangedClasspathEntries.removeAll(allTestSourceClasspathEntries);

				aJavaProject.setRawClasspath(allChangedClasspathEntries.toArray(new IClasspathEntry[allChangedClasspathEntries.size()]), null);

				createTestProject(theProject, theTestType, allTestSourceClasspathEntries);
			}
		}
		catch (JavaModelException theCause) {
			error.println("Could not access class path of project '" + theProject.getName() + "': " + theCause.getMessage());
		}

	}

	private void createTestProject(IProject theProject, String theTestType, List<IClasspathEntry> theTestSourceClasspathEntries) {
		IWorkspace aWorkspace = theProject.getWorkspace();
		String aTestProjectName = theProject.getName() + "." + theTestType;
		ProjectWrapper aProjectWrapper = ProjectWrapper
				.of(aWorkspace, aTestProjectName);

		if (!aProjectWrapper.isExisting()) {
			output.println(" -> Create OSGi Test fragment project: " + aTestProjectName);
			IPath aWorkspaceLocation = theProject.getWorkspace().getRoot().getLocation();
			aProjectWrapper.createProject()
					.open()
					.toJavaProject()
					.removeDefaultSourceFolder()
					.setOutputFolder("bin")
					.addNature(ProjectConstants.PLUGIN_NATURE_ID)
					.addBuilder("org.eclipse.pde.ManifestBuilder")
					.addBuilder("org.eclipse.pde.SchemaBuilder")
					.addClasspathEntry(
							theTestProject -> JavaCore.newContainerEntry(
									new Path("org.eclipse.jdt.launching.JRE_CONTAINER/org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType/JavaSE-1.8")))
					.addClasspathEntry(
							theTestProject -> JavaCore.newContainerEntry(
									new Path(ProjectConstants.PLUGIN_CLASSPATH_ID)));

			IPath aProjectLocation = theProject.getLocation();
			for (IClasspathEntry aTestSourceClasspathEntry : theTestSourceClasspathEntries) {
				IPath aRelativeProjectLocation = aProjectLocation.makeRelativeTo(aWorkspaceLocation);
				IPath aSourceLocation = new Path("WORKSPACE_LOC").append(aRelativeProjectLocation).append(aTestSourceClasspathEntry.getPath().removeFirstSegments(1));

				if (aTestSourceClasspathEntry.getPath().lastSegment().equals("java")) {
					aProjectWrapper.addLinkedSourceFolder("test-java", aSourceLocation);
				}
				if (aTestSourceClasspathEntry.getPath().lastSegment().equals("resources")) {
					aProjectWrapper.addLinkedSourceFolder("test-resources", aSourceLocation);
				}
			}

			aProjectWrapper
					.createTestFragmentManifest(theProject, () -> {
						Set<String> someAdditionalPackages = new HashSet<>();
						// package org.hamcrest is opften used to run unit tests
						someAdditionalPackages.add("org.hamcrest");
						Optional.ofNullable(additionalTestFragmentDependencies.get(aTestProjectName)).ifPresent(thePackages -> someAdditionalPackages.addAll(thePackages));

						return someAdditionalPackages;
					}, () -> Optional.ofNullable(ignoreTestFragmentDependencies.get(aTestProjectName)).orElse(Collections.emptySet()))
					.createBuildProperties()
					.refresh();

			// Some of the Xentis projects have now set the encoding UTF-8 which is not the default.
			// Therefore the corresponding test fragment should have the same encoding
			try {
				String aTestCharset = aProjectWrapper.getProject().getDefaultCharset();
				String aHostCharset = theProject.getDefaultCharset();

				if (!aHostCharset.equals(aTestCharset)) {
					aProjectWrapper.getProject().setDefaultCharset(aHostCharset, null);
				}
			}
			catch (CoreException theCause) {
				error.println("Access to default charset of project '" + aTestProjectName + "' failed:\n-> " + aProjectWrapper.getErrorMessage());
			}

			if (aProjectWrapper.hasError()) {
				error.println("Create test project '" + aTestProjectName + "' failed:\n-> " + aProjectWrapper.getErrorMessage());
			}
		}
	}

	private boolean hasNature(IProject theProject, String theNatureId) {
		try {
			return theProject.hasNature(theNatureId);
		}
		catch (CoreException theCause) {
			return false;
		}
	}

	private IProjectDescription getProjectDescription(IProject theProject) {
		try {
			return theProject.getDescription();
		}
		catch (CoreException theCause) {
			return null;
		}
	}

	private void setProjectDescription(IProject theProject, IProjectDescription theProjectDescription) {
		try {
			theProject.setDescription(theProjectDescription, null);
		}
		catch (CoreException theCause) {
			error.println("Could not set project description: " + theCause.getMessage());
		}
	}
}
