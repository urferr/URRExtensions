package com.profidata.xentis.fix;

import java.io.PrintStream;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

import org.eclipse.core.resources.FileInfoMatcherDescription;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceFilterDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;

public class IgnoreProjectFolder {
	private final PrintStream output;
	private final PrintStream error;

	private final String projectFolder;

	public static void run(String theProjectFolder, PrintStream theOutput, PrintStream theError) {
		new IgnoreProjectFolder(theProjectFolder, theOutput, theError).execute();
	}

	private IgnoreProjectFolder(String theProjectFolder, PrintStream theOutput, PrintStream theError) {
		projectFolder = theProjectFolder;
		output = theOutput;
		error = theError;
	}

	private void execute() {
		IWorkspace aWorkspace = ResourcesPlugin.getWorkspace();

		findProjectsWithFolder(aWorkspace, projectFolder).stream()
				.forEach(theProject -> {
					output.println(MessageFormat.format("ignore ''{0}'' in project ''{1}''", projectFolder, theProject.getName()));
					try {
						theProject.createFilter(
								IResourceFilterDescription.EXCLUDE_ALL | IResourceFilterDescription.FOLDERS | IResourceFilterDescription.INHERITABLE,
								new FileInfoMatcherDescription(
										"org.eclipse.ui.ide.multiFilter",
										"1.0-name-matches-false-false-" + projectFolder),
								0,
								null);
					}
					catch (CoreException theCause) {
						error.println(" -> failed: " + theCause.getMessage());
					}
				});
	}

	private Collection<IProject> findProjectsWithFolder(IWorkspace theWorkspace, String theFolder) {
		return Arrays.stream(theWorkspace.getRoot().getProjects())
				.filter(theProject -> hasFolder(theProject, theFolder))
				.collect(Collectors.toSet());
	}

	private boolean hasFolder(IProject theProject, String theFolder) {
		return theProject.getFolder(theFolder).exists();
	}
}
