package com.profidata.xentis.fix;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

public class RemoveGradleNatureFromPlugins {
	private static final String PLUGIN_NATURE_ID = "org.eclipse.pde.PluginNature";
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
					out.println("Project with double classpath nature: " + theProject.getName());
					removeGradleNature(aWorkspace, theProject);
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

	private void removeGradleNature(IWorkspace theWorkspace, IProject theProject) {
		IProjectDescription aProjectDescription = getProjectDescription(theProject);

		if (aProjectDescription != null) {
			String[] someResultingNatures = Arrays.stream(aProjectDescription.getNatureIds())
					.filter(theNatureId -> !theNatureId.equals(GRADLE_NATURE_ID))
					.toArray(String[]::new);
			IStatus aStatus = theWorkspace.validateNatureSet(someResultingNatures);

			if (aStatus.getCode() == IStatus.OK) {
				aProjectDescription.setNatureIds(someResultingNatures);
				setProjectDescription(theProject, aProjectDescription);
				removeGradleClasspathContainer(theProject);
				try {
					theProject.build(IncrementalProjectBuilder.FULL_BUILD, null);
				}
				catch (CoreException theCause) {
					err.println("Could not build project '" + theProject.getName() + "'" + theCause.getMessage());
				}
			}
		}
	}

	private void removeGradleClasspathContainer(IProject theProject) {
		IJavaProject aJavaProject = JavaCore.create(theProject);

		try {
			IClasspathEntry[] allClasspathEntries = aJavaProject.getRawClasspath();
			IClasspathEntry[] allChangedClasspathEntries = Arrays.stream(allClasspathEntries)
					.filter(theEntry -> !theEntry.getPath().toString().equals(GRADLE_CLASSPATH_ID))
					.toArray(IClasspathEntry[]::new);

			if (allClasspathEntries.length != allChangedClasspathEntries.length) {
				aJavaProject.setRawClasspath(allChangedClasspathEntries, null);
			}
		}
		catch (JavaModelException theCause) {
			err.println("Could not access class path of project '" + theProject.getName() + "'" + theCause.getMessage());
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
