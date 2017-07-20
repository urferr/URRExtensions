package com.profidata.xentis.fix;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
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
				removeTestSourceFolder(theProject);
				try {
					theProject.build(IncrementalProjectBuilder.FULL_BUILD, null);
				}
				catch (CoreException theCause) {
					err.println("Could not build project '" + theProject.getName() + "': " + theCause.getMessage());
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
			err.println("Could not access class path of project '" + theProject.getName() + "': " + theCause.getMessage());
		}

	}

	private void removeTestSourceFolder(IProject theProject) {
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

	private void createTestProject(IProject theProject, List<IClasspathEntry> theAllTestSourceClasspathEntries) {
		IWorkspace aWorkspace = theProject.getWorkspace();
		String aTestProjectName = theProject.getName() + ".test";

		IProject aTestProject = aWorkspace.getRoot().getProject(aTestProjectName);

		if (aTestProject.exists()) {
			return;
		}

		out.println("Create Test project: " + aTestProjectName);
		IJavaProject aTestJavaProject = JavaCore.create(theProject);
		IProjectDescription aTestProjectDescription = aWorkspace.newProjectDescription(aTestProjectName);

		try {
			aTestProjectDescription.setLocation(null);
			aTestProject.create(aTestProjectDescription, null);
			aTestProject.open(null);

			addTestProjectNatures(aTestProjectDescription);
			addTestProjectBuilders(aTestProjectDescription);
			addTestProjectClasspath(aTestJavaProject);
			addSourceLinkToTestSource(aTestJavaProject, theProject, theAllTestSourceClasspathEntries);

			aTestProject.setDescription(aTestProjectDescription, null);
		}
		catch (CoreException theCause) {
			err.println("Could not properly create test project '" + aTestProjectName + "': " + theCause.getMessage());
		}
	}

	private void addTestProjectNatures(IProjectDescription theTestProjectDescription) {
		String[] allTestProjectNatures = new String[] {
				JavaCore.NATURE_ID,
				"org.eclipse.pde.PluginNature" };

		theTestProjectDescription.setNatureIds(allTestProjectNatures);
	}

	private void addTestProjectBuilders(IProjectDescription theTestProjectDescription) {
		ICommand aJavaBuilder = theTestProjectDescription.newCommand();
		ICommand aManifestBuilder = theTestProjectDescription.newCommand();
		ICommand aSchemaBuilder = theTestProjectDescription.newCommand();

		aJavaBuilder.setBuilderName(JavaCore.BUILDER_ID);
		aManifestBuilder.setBuilderName("org.eclipse.pde.ManifestBuilder");
		aSchemaBuilder.setBuilderName("org.eclipse.pde.SchemaBuilder");

		theTestProjectDescription.setBuildSpec(
				new ICommand[] {
						aJavaBuilder,
						aManifestBuilder,
						aSchemaBuilder });
	}

	private void addTestProjectClasspath(IJavaProject theTestJavaProject) {
		IClasspathEntry[] allClasspathEntries = new IClasspathEntry[] {
				JavaCore.newSourceEntry(new Path("/" + theTestJavaProject.getProject().getName() + "/src")),
				JavaCore.newContainerEntry(
						new Path("org.eclipse.jdt.launching.JRE_CONTAINER/org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType/JavaSE-1.8")),
				JavaCore.newContainerEntry(
						new Path("org.eclipse.pde.core.requiredPlugins")) };

		try {
			theTestJavaProject.getRawClasspath();
			theTestJavaProject.setRawClasspath(allClasspathEntries, null);
			theTestJavaProject.setOutputLocation(new Path("/" + theTestJavaProject.getProject().getName() + "/bin"), null);
		}
		catch (JavaModelException theCause) {
			err.println("Could set the class path to project '" + theTestJavaProject.getProject().getName() + "': " + theCause.getMessage());
		}
	}

	private void addSourceLinkToTestSource(IJavaProject theTestJavaProject, IProject theSourceProject, List<IClasspathEntry> theTestSourceClasspathEntries) {
		for (IClasspathEntry aTestSourceClasspathEntry : theTestSourceClasspathEntries) {
			out.println(aTestSourceClasspathEntry.getPath());
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
