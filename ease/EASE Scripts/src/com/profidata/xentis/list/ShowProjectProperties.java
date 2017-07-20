package com.profidata.xentis.list;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Arrays;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

public class ShowProjectProperties {
	private static final String INDENT = "   ";

	private static PrintStream out;
	private static PrintStream err;

	public static void initialize(InputStream in, PrintStream out, PrintStream error) {
		ShowProjectProperties.out = out;
		ShowProjectProperties.err = error;
	}

	public static void main(String[] args) throws Exception {
		new ShowProjectProperties().execute();
	}

	private void execute() {
		IWorkspace aWorkspace = ResourcesPlugin.getWorkspace();

		listWorkspace(aWorkspace);
		Arrays.stream(aWorkspace.getRoot().getProjects())
				.forEach(theProject -> {
					listProject(theProject);
					listNature(theProject);
					listClasspath(theProject);
				});
	}

	private void listWorkspace(IWorkspace theWorkspace) {
		out.println("Workspace:");
		out.println(INDENT + "Location: " + theWorkspace.getRoot().getLocation());
	}

	private void listProject(IProject theProject) {
		out.println("Project: " + theProject.getName());
		out.println(INDENT + "Location: " + theProject.getLocation());
	}

	private void listNature(IProject theProject) {
		IProjectDescription aProjectDescription = getProjectDescription(theProject);

		if (aProjectDescription != null) {
			out.println(INDENT + "Natures:");
			Arrays.stream(aProjectDescription.getNatureIds())
					.forEach(theNatureId -> out.println(INDENT + "- " + theNatureId));
		}
	}

	private void listClasspath(IProject theProject) {
		IJavaProject aJavaProject = JavaCore.create(theProject);

		try {
			out.println(INDENT + "Classpath entries:");
			Arrays.stream(aJavaProject.getRawClasspath())
					.forEach(
							theClasspathEntry -> out.println(
									INDENT + "- " + theClasspathEntry.getPath() +
											" - " + "Entry: " + toEntryKind(theClasspathEntry.getEntryKind()) +
											" - " + "Content: " + toContentKind(theClasspathEntry.getContentKind())));
			out.println(INDENT + "- " + aJavaProject.getOutputLocation() + " - " + "output");
		}
		catch (JavaModelException theCause) {
			err.println("Could not access class path of project '" + theProject.getName() + "': " + theCause.getMessage());
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

	private String toContentKind(int theContentKindId) {
		switch (theContentKindId) {
			case IPackageFragmentRoot.K_SOURCE:
				return "source";
			case IPackageFragmentRoot.K_BINARY:
				return "binary";
			default:
				return "unknown(" + theContentKindId + ")";
		}
	}

	private String toEntryKind(int theEntryKindId) {
		switch (theEntryKindId) {
			case IClasspathEntry.CPE_SOURCE:
				return "source";
			case IClasspathEntry.CPE_LIBRARY:
				return "library";
			case IClasspathEntry.CPE_PROJECT:
				return "project";
			case IClasspathEntry.CPE_VARIABLE:
				return "variable";
			case IClasspathEntry.CPE_CONTAINER:
				return "container";
			default:
				return "unknown(" + theEntryKindId + ")";
		}
	}
}
