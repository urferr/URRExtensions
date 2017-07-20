package com.profidata.xentis.trial;

import java.io.InputStream;
import java.io.PrintStream;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.JavaCore;

import com.profidata.xentis.util.ProjectWrapper;

public class CreateDummyJavaProject {
	private static final String PLUGIN_NATURE_ID = "org.eclipse.pde.PluginNature";
	private static final String GRADLE_NATURE_ID = "org.eclipse.buildship.core.gradleprojectnature";
	private static final String GRADLE_CLASSPATH_ID = "org.eclipse.buildship.core.gradleclasspathcontainer";

	private static PrintStream out;
	private static PrintStream err;

	public static void initialize(InputStream in, PrintStream out, PrintStream error) {
		CreateDummyJavaProject.out = out;
		CreateDummyJavaProject.err = error;
	}

	public static void main(String[] args) throws Exception {
		new CreateDummyJavaProject().execute();
	}

	private void execute() {
		IWorkspace aWorkspace = ResourcesPlugin.getWorkspace();

		createJavaProject(aWorkspace);
		createPluginProject(aWorkspace);

		try {
			aWorkspace.getRoot().refreshLocal(IResource.DEPTH_INFINITE, null);
		}
		catch (CoreException theCause) {
			err.println("Could not refresh resources: " + theCause.getMessage());
		}
	}

	private void createJavaProject(IWorkspace theWorkspace) {
		ProjectWrapper aProjectWrapper = ProjectWrapper
				.of(theWorkspace, "dummy.java.ease")
				.createProject()
				.toJavaProject()
				.addSourceFolder("src")
				.addSourceFolder("conf")
				.setOutputFolder("bin")
				.addClasspathEntry(
						theProject -> JavaCore.newContainerEntry(
								new Path("org.eclipse.jdt.launching.JRE_CONTAINER/org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType/JavaSE-1.8")))
				.createPackage("src", "com.test")
				.createTestClass("src", "com.test");

		if (aProjectWrapper.hasError()) {
			err.println("Creation of project 'dummy.java.ease' failed:\n-> " + aProjectWrapper.getErrorMessage());
		}
		else {
			out.println("Creation of project 'dummy.java.ease' successful");
		}
	}

	private void createPluginProject(IWorkspace theWorkspace) {
		ProjectWrapper aProjectWrapper = ProjectWrapper
				.of(theWorkspace, "dummy.plugin.ease")
				.createProject()
				.toJavaProject()
				.addSourceFolder("src")
				.addLinkedSourceFolder("src-link", new Path("WORKSPACE_LOC/dummy.java.ease/src"))
				.setOutputFolder("bin")
				.addNature("org.eclipse.pde.PluginNature")
				.addBuilder("org.eclipse.pde.ManifestBuilder")
				.addBuilder("org.eclipse.pde.SchemaBuilder")
				.addClasspathEntry(
						theProject -> JavaCore.newContainerEntry(
								new Path("org.eclipse.jdt.launching.JRE_CONTAINER/org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType/JavaSE-1.8")))
				.addClasspathEntry(
						theProject -> JavaCore.newContainerEntry(
								new Path("org.eclipse.pde.core.requiredPlugins")));

		if (aProjectWrapper.hasError()) {
			err.println("Creation of project 'dummy.plugin.ease' failed:\n-> " + aProjectWrapper.getErrorMessage());
		}
		else {
			out.println("Creation of project 'dummy.plugin.ease' successful");
		}
	}
}
