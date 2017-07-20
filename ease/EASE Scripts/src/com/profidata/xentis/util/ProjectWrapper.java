package com.profidata.xentis.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

public class ProjectWrapper {
	private final IProject project;
	private IJavaProject javaProject;

	private String errorMessage;

	public static ProjectWrapper of(IWorkspace theWorkspace, String theName) {
		return new ProjectWrapper(theWorkspace.getRoot().getProject(theName));
	}

	private ProjectWrapper(IProject theProject) {
		project = Objects.requireNonNull(theProject);
	}

	public ProjectWrapper createProject() {
		if (!project.exists()) {
			try {
				project.create(null);
				project.open(null);
			}
			catch (CoreException theCause) {
				errorMessage = "Could not create project '" + project.getName() + "': " + theCause.getMessage();
			}
		}
		else {
			errorMessage = "Project '" + project.getName() + "' already exists";
		}
		return this;
	}

	public ProjectWrapper toJavaProject() {
		if (!hasError()) {
			addNature(JavaCore.NATURE_ID);
			javaProject = JavaCore.create(project);
		}
		return this;
	}

	public ProjectWrapper addNature(String theNatureId) {
		if (!hasError()) {
			try {
				if (!project.hasNature(theNatureId)) {
					IProjectDescription aProjectDescription = project.getDescription();
					List<String> allNatureIds = new ArrayList<>(Arrays.asList(aProjectDescription.getNatureIds()));

					allNatureIds.add(theNatureId);
					aProjectDescription.setNatureIds(allNatureIds.toArray(new String[allNatureIds.size()]));
					project.setDescription(aProjectDescription, null);
				}
			}
			catch (CoreException theCause) {
				errorMessage = "Could not add nature '" + theNatureId + "' to project '" + project.getName() + "': " + theCause.getMessage();
			}
		}
		return this;
	}

	public ProjectWrapper addBuilder(String theBuilderId) {
		if (!hasError()) {
			try {
				ICommand aBuilderCommand = project.getDescription().newCommand();
				aBuilderCommand.setBuilderName(theBuilderId);
			}
			catch (CoreException theCause) {
				errorMessage = "Could not add builder '" + theBuilderId + "' to project '" + project.getName() + "': " + theCause.getMessage();
			}
		}
		return this;
	}

	public ProjectWrapper addSourceFolder(String theFolderName) {
		verifyJavaProject();

		try {
			Arrays.asList(javaProject.getRawClasspath()).stream()
					.filter(theClasspathEntry -> theClasspathEntry.getPath().equals(project.getFullPath()))
					.findFirst()
					.ifPresent(theClasspathEntry -> removeClasspathEntry(theProject -> theClasspathEntry));
		}
		catch (JavaModelException theCause) {
			errorMessage = "Could not veriy if source folder '" + theFolderName + "' already exists in Java project '" + project.getName() + "': " + theCause.getMessage();
		}

		if (!hasError()) {
			IPath aClasspath = project.getFullPath().append(theFolderName);

			addClasspathEntry(theProject -> JavaCore.newSourceEntry(aClasspath));
		}
		if (!hasError()) {
			try {
				IFolder folder = project.getFolder(theFolderName);
				folder.create(true, true, null);
			}
			catch (CoreException theCause) {
				errorMessage = "Could not create source folder '" + theFolderName + "' in Java project '" + project.getName() + "': " + theCause.getMessage();
			}
		}
		return this;
	}

	public ProjectWrapper setOutputFolder(String theFolderName) {
		verifyJavaProject();

		if (!hasError()) {
			IFolder aBinaryFolder = project.getFolder(theFolderName);

			try {
				aBinaryFolder.create(false, true, null);
				javaProject.setOutputLocation(aBinaryFolder.getFullPath(), null);
			}
			catch (CoreException theCause) {
				errorMessage = "Could not set output folder '" + theFolderName + "' in Java project '" + project.getName() + "': " + theCause.getMessage();
			}
		}
		return this;
	}

	public ProjectWrapper addClasspathEntry(Function<IProject, IClasspathEntry> theClasspathEntrySupplier) {
		verifyJavaProject();

		if (!hasError()) {
			IClasspathEntry aClasspathEntry = theClasspathEntrySupplier.apply(project);
			try {
				List<IClasspathEntry> allClasspathEntries = new ArrayList<>(Arrays.asList(javaProject.getRawClasspath()));

				allClasspathEntries.add(aClasspathEntry);
				javaProject.setRawClasspath(allClasspathEntries.toArray(new IClasspathEntry[allClasspathEntries.size()]), null);
			}
			catch (JavaModelException theCause) {
				errorMessage = "Could not add classpath entry '" + aClasspathEntry + "' to Java project '" + project.getName() + "': " + theCause.getMessage();
			}
		}
		return this;
	}

	public ProjectWrapper removeClasspathEntry(Function<IProject, IClasspathEntry> theClasspathEntrySupplier) {
		verifyJavaProject();

		if (!hasError()) {
			IClasspathEntry aClasspathEntry = theClasspathEntrySupplier.apply(project);
			try {
				List<IClasspathEntry> allClasspathEntries = new ArrayList<>(Arrays.asList(javaProject.getRawClasspath())).stream()
						.filter(theClasspathEntry -> !theClasspathEntry.getPath().equals(aClasspathEntry.getPath()))
						.collect(Collectors.toList());

				javaProject.setRawClasspath(allClasspathEntries.toArray(new IClasspathEntry[allClasspathEntries.size()]), null);
			}
			catch (JavaModelException theCause) {
				errorMessage = "Could not remove classpath entry '" + aClasspathEntry + "' to Java project '" + project.getName() + "': " + theCause.getMessage();
			}
		}
		return this;
	}

	public ProjectWrapper createPackage(String theFolder, String thePackage) {
		verifyJavaProject();

		if (!hasError()) {
			IPackageFragmentRoot aSourceFolder = javaProject.getPackageFragmentRoot(project.getFolder(theFolder));
			try {
				aSourceFolder.createPackageFragment(thePackage, true, null);
			}
			catch (JavaModelException theCause) {
				errorMessage = "Could not create package '" + thePackage + "' in source folder '" + theFolder + "': " + theCause.getMessage();
			}
		}
		return this;
	}

	private void verifyJavaProject() {
		if (!hasError() && javaProject == null) {
			errorMessage = "project '" + project.getName() + "' is not a Java project";
		}
	}

	public boolean hasError() {
		return errorMessage != null;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public ProjectWrapper createTestClass(String theSourceFolder, String thePackage) {
		if (!hasError()) {
			try {

				// get folder by using resources package
				IFolder aFolder = project.getFolder(theSourceFolder);

				// Add folder to Java element
				IPackageFragmentRoot srcFolder = javaProject.getPackageFragmentRoot(aFolder);

				// get package fragment
				IPackageFragment fragment = srcFolder.getPackageFragment(thePackage);

				//init code string and create compilation unit
				String str = "package " + thePackage + ";" + "\n"
						+ "public class Test  {" + "\n" + " private String name;"
						+ "\n" + "}";

				ICompilationUnit cu = fragment.createCompilationUnit(
						"Test.java",
						str,
						false,
						null);

				//create a field
				IType type = cu.getType("Test");

				type.createField("private String age;", null, true, null);
			}
			catch (CoreException theCause) {
				errorMessage = "Could not create test class: " + theCause.getMessage();
			}
		}

		return this;
	}
}
