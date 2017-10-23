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
	private static final Map<String, Set<String>> ignoreTestFragmentDependencies;

	static {
		Set<String> somePackages;

		ignoreTestFragmentDependencies = new HashMap<>();

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
							.refresh();

					if (aProjectWrapper.hasError()) {
						error.println("Fix project '" + theProject.getName() + "' failed:\n-> " + aProjectWrapper.getErrorMessage());
					}
					else if (!theProject.getName().endsWith("-integration")) {
						migrateTestSourceFolderToTestFragmentProject(theProject, Arrays.asList("test", "integration", "manual"));
					}
				});

		findTestFragmentProjects(aWorkspace).stream()
				.forEach(theProject -> {
					output.println("Upgrade package dependencies of test fragment: " + theProject.getName());
					ProjectWrapper aProjectWrapper = ProjectWrapper.of(theProject)
							.asJavaProject()
							.createTestFragmentPackageDependencies(aWorkspace, () -> {
								Set<String> someAdditionalPackages = new HashSet<>();
								// package org.hamcrest is opften used to run unit tests
								someAdditionalPackages.add("org.hamcrest;core=split");

								return someAdditionalPackages;
							}, () -> Optional.ofNullable(ignoreTestFragmentDependencies.get(theProject.getName())).orElse(Collections.emptySet()))
							.refresh();

					if (aProjectWrapper.hasError()) {
						error.println("Upgrade package dependencies of test fragment '" + theProject.getName() + "' failed:\n-> " + aProjectWrapper.getErrorMessage());
					}
				});
	}

	private Collection<IProject> findProjectsWithPluginAndGradleNature(IWorkspace theWorkspace) {
		return Arrays.stream(theWorkspace.getRoot().getProjects())
				.filter(theProject -> hasNature(theProject, ProjectConstants.PLUGIN_NATURE_ID) && hasNature(theProject, ProjectConstants.GRADLE_NATURE_ID))
				.collect(Collectors.toSet());
	}

	private Collection<IProject> findTestFragmentProjects(IWorkspace theWorkspace) {
		return Arrays.stream(theWorkspace.getRoot().getProjects())
				.filter(
						theProject -> hasNature(theProject, ProjectConstants.PLUGIN_NATURE_ID) && !hasNature(theProject, ProjectConstants.GRADLE_NATURE_ID) && isFragment(theProject)
								&& theProject.getName().endsWith(".test"))
				.collect(Collectors.toSet());
	}

	private void migrateTestSourceFolderToTestFragmentProject(IProject theProject, Collection<String> theTestTypes) {
		IJavaProject aJavaProject = JavaCore.create(theProject);

		try {
			IClasspathEntry[] allClasspathEntries = aJavaProject.getRawClasspath();
			List<IClasspathEntry> allTestSourceClasspathEntries;

			// first we check for unit tests
			allTestSourceClasspathEntries = Arrays.stream(allClasspathEntries)
					.filter(theEntry -> theEntry.getContentKind() == IPackageFragmentRoot.K_SOURCE && theEntry.getEntryKind() == IClasspathEntry.CPE_SOURCE)
					.filter(
							theEntry -> theTestTypes.contains(theEntry.getPath().removeFirstSegments(1).segment(0))
									|| (theEntry.getPath().removeFirstSegments(1).segmentCount() > 1 && theEntry.getPath().removeFirstSegments(1).segment(0).equals("src")
											&& theTestTypes.contains(theEntry.getPath().removeFirstSegments(1).segment(1))))
					.collect(Collectors.toList());

			if (!allTestSourceClasspathEntries.isEmpty()) {
				List<IClasspathEntry> allChangedClasspathEntries = new ArrayList<>(Arrays.asList(allClasspathEntries));

				allChangedClasspathEntries.removeAll(allTestSourceClasspathEntries);

				aJavaProject.setRawClasspath(allChangedClasspathEntries.toArray(new IClasspathEntry[allChangedClasspathEntries.size()]), null);

				createTestProject(theProject, allTestSourceClasspathEntries);
			}
		}
		catch (JavaModelException theCause) {
			error.println("Could not access class path of project '" + theProject.getName() + "': " + theCause.getMessage());
		}

	}

	private void createTestProject(IProject theProject, List<IClasspathEntry> theTestSourceClasspathEntries) {
		IWorkspace aWorkspace = theProject.getWorkspace();
		String aTestProjectName = theProject.getName() + ".test";
		ProjectWrapper aProjectWrapper = ProjectWrapper
				.of(aWorkspace, aTestProjectName);

		if (!aProjectWrapper.isExisting()) {
			output.println(" -> Create OSGi Test fragment project: " + aTestProjectName);
			ProjectWrapper.of(theProject).setSingletonPlugin(true);

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
				IPath aSourcePath = aTestSourceClasspathEntry.getPath();
				IPath aSourceLocation = new Path("WORKSPACE_LOC").append(aRelativeProjectLocation).append(aSourcePath.removeFirstSegments(1));
				String aSourceType = aSourcePath.lastSegment();
				String aTestType = aSourcePath.removeLastSegments(1).lastSegment();

				aProjectWrapper.addLinkedSourceFolder(aTestType + "-" + aSourceType, aSourceLocation);
			}

			aProjectWrapper
					.createTestFragmentManifest(theProject)
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

	private boolean isFragment(IProject theProject) {
		return ProjectWrapper.of(theProject).isFragment();
	}

}
