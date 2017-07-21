package com.profidata.xentis.scripts;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Collections;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.JavaCore;

import com.profidata.xentis.fix.RemoveGradleNatureFromPlugins;
import com.profidata.xentis.util.ProjectConstants;
import com.profidata.xentis.util.ProjectWrapper;

public class XentisWorkspace {
	private static PrintStream output;
	private static PrintStream error;

	public static void initialize(InputStream theInput, PrintStream theOuotput, PrintStream theError) {
		XentisWorkspace.output = theOuotput;
		XentisWorkspace.error = theError;
	}

	public static void main(String[] args) throws Exception {
		new XentisWorkspace().execute();
	}

	private void execute() {
		IWorkspace aWorkspace = ResourcesPlugin.getWorkspace();

		output.println("Fix projects with Plugin/Gradle nature");
		output.println("======================================");
		RemoveGradleNatureFromPlugins.run(output, error);

		output.println("");
		output.println("Exchange Gradle with Plugin nature");
		output.println("==================================");
		convertProjectFromGradleToPlugin(aWorkspace, "com.profidata.xentis.test");

		output.println("");
		output.println("Import missing projects");
		output.println("=======================");

		try {
			aWorkspace.getRoot().refreshLocal(IResource.DEPTH_INFINITE, null);
		}
		catch (CoreException theCause) {
			error.print("Could not refresh workspace: " + theCause.getMessage());
		}
	}

	private void convertProjectFromGradleToPlugin(IWorkspace theWorkspace, String theProjectName) {
		ProjectWrapper aProjectWrapper = ProjectWrapper.of(theWorkspace, theProjectName);

		if (aProjectWrapper.hasNature(ProjectConstants.GRADLE_NATURE_ID)) {
			output.println("Exchange Gradle with Plugin nature for project: " + theProjectName);
			aProjectWrapper
					.toJavaProject()
					.removeClasspathEntry(new Path(ProjectConstants.GRADLE_CLASSPATH_ID))
					.removeNature(ProjectConstants.GRADLE_NATURE_ID)
					.addNature(ProjectConstants.PLUGIN_NATURE_ID)
					.addClasspathEntry(theProject -> JavaCore.newContainerEntry(new Path(ProjectConstants.PLUGIN_CLASSPATH_ID)))
					.createPluginManifest(() -> Collections.emptySet())
					.refresh()
					.build();
		}
		if (aProjectWrapper.hasError()) {
			error.println("Exchange Gradle with Plugin nature for project: " + theProjectName + "' failed:\n-> " + aProjectWrapper.getErrorMessage());
		}
	}
}
