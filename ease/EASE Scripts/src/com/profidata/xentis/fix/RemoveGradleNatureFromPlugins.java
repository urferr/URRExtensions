package com.profidata.xentis.fix;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
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

import com.profidata.xentis.util.ProjectWrapper;

public class RemoveGradleNatureFromPlugins {
	private static final String PLUGIN_NATURE_ID = "org.eclipse.pde.PluginNature";
	private static final String PLUGIN_CLASSPATH_ID = "org.eclipse.pde.core.requiredPlugins";
	private static final String GRADLE_NATURE_ID = "org.eclipse.buildship.core.gradleprojectnature";
	private static final String GRADLE_CLASSPATH_ID = "org.eclipse.buildship.core.gradleclasspathcontainer";

	private static PrintStream out;
	private static PrintStream err;

	public static void initialize(InputStream in, PrintStream out, PrintStream error) {
		RemoveGradleNatureFromPlugins.out = out;
		RemoveGradleNatureFromPlugins.err = error;
	}

	public static void main(String[] args) throws Exception {
		new RemoveGradleNatureFromPlugins().execute();
	}

	private void execute() {
		IWorkspace aWorkspace = ResourcesPlugin.getWorkspace();

		findProjectsWithPluginAndGradleNature(aWorkspace).stream()
				.forEach(theProject -> {
					out.println("Fix project with Plugin/Gradle nature: " + theProject.getName());
					ProjectWrapper aProjectWrapper = ProjectWrapper.of(theProject)
							.toJavaProject()
							.removeNature(GRADLE_NATURE_ID)
							.removeClasspathEntry(new Path(GRADLE_CLASSPATH_ID))
							.build();

					if (aProjectWrapper.hasError()) {
						err.println("Fix project '" + theProject.getName() + "' failed:\n-> " + aProjectWrapper.getErrorMessage());
					}
					else {
						migrateTestSourceFolderToTestFragmentProject(theProject);
					}
				});

		try {
			aWorkspace.getRoot().refreshLocal(IResource.DEPTH_INFINITE, null);
		}
		catch (CoreException theCause) {
			err.print("Could not refresh resource: " + theCause.getMessage());
		}
	}

	private Collection<IProject> findProjectsWithPluginAndGradleNature(IWorkspace theWorkspace) {
		return Arrays.stream(theWorkspace.getRoot().getProjects())
				.filter(theProject -> hasNature(theProject, PLUGIN_NATURE_ID) && hasNature(theProject, GRADLE_NATURE_ID))
				.collect(Collectors.toSet());
	}

	private void migrateTestSourceFolderToTestFragmentProject(IProject theProject) {
		IJavaProject aJavaProject = JavaCore.create(theProject);

		try {
			IClasspathEntry[] allClasspathEntries = aJavaProject.getRawClasspath();
			List<IClasspathEntry> allTestSourceClasspathEntries = Arrays.stream(allClasspathEntries)
					.filter(theEntry -> theEntry.getContentKind() == IPackageFragmentRoot.K_SOURCE && theEntry.getEntryKind() == IClasspathEntry.CPE_SOURCE)
					.filter(theEntry -> theEntry.getPath().removeFirstSegments(1).segment(0).equals("test"))
					.collect(Collectors.toList());

			if (!allTestSourceClasspathEntries.isEmpty()) {
				List<IClasspathEntry> allChangedClasspathEntries = new ArrayList<>(Arrays.asList(allClasspathEntries));

				allChangedClasspathEntries.removeAll(allTestSourceClasspathEntries);

				aJavaProject.setRawClasspath(allChangedClasspathEntries.toArray(new IClasspathEntry[allChangedClasspathEntries.size()]), null);

				createTestProject(theProject, allTestSourceClasspathEntries);
			}
		}
		catch (JavaModelException theCause) {
			err.println("Could not access class path of project '" + theProject.getName() + "': " + theCause.getMessage());
		}

	}

	private void createTestProject(IProject theProject, List<IClasspathEntry> theTestSourceClasspathEntries) {
		IWorkspace aWorkspace = theProject.getWorkspace();
		String aTestProjectName = theProject.getName() + ".test";

		out.println(" -> Create Test project: " + aTestProjectName);
		ProjectWrapper aProjectWrapper = ProjectWrapper
				.of(aWorkspace, aTestProjectName)
				.createProject()
				.toJavaProject()
				.removeDefaultSourceFolder()
				.setOutputFolder("bin")
				.addNature(PLUGIN_NATURE_ID)
				.addBuilder("org.eclipse.pde.ManifestBuilder")
				.addBuilder("org.eclipse.pde.SchemaBuilder")
				.addClasspathEntry(
						theTestProject -> JavaCore.newContainerEntry(
								new Path("org.eclipse.jdt.launching.JRE_CONTAINER/org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType/JavaSE-1.8")))
				.addClasspathEntry(
						theTestProject -> JavaCore.newContainerEntry(
								new Path(PLUGIN_CLASSPATH_ID)));

		IPath aWorkspaceLocation = theProject.getWorkspace().getRoot().getLocation();
		IPath aProjectLocation = theProject.getLocation();
		for (IClasspathEntry aTestSourceClasspathEntry : theTestSourceClasspathEntries) {
			out.println(Arrays.toString(aTestSourceClasspathEntry.getPath().segments()));

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
				.createFragmentManifest(theProject)
				.createFragmentBuildProperties();

		if (aProjectWrapper.hasError()) {
			err.println("Create test project '" + aTestProjectName + "' failed:\n-> " + aProjectWrapper.getErrorMessage());
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
			err.println("Could not set project description: " + theCause.getMessage());
		}
	}
}
