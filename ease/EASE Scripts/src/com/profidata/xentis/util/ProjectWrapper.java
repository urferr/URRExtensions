package com.profidata.xentis.util;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IParent;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.pde.core.IEditableModel;
import org.eclipse.pde.core.build.IBuildEntry;
import org.eclipse.pde.core.build.IBuildModelFactory;
import org.eclipse.pde.core.plugin.IFragmentModel;
import org.eclipse.pde.core.plugin.IPluginModel;
import org.eclipse.pde.internal.core.build.WorkspaceBuildModel;
import org.eclipse.pde.internal.core.bundle.WorkspaceBundleFragmentModel;
import org.eclipse.pde.internal.core.bundle.WorkspaceBundlePluginModel;
import org.eclipse.pde.internal.core.ibundle.IBundle;
import org.eclipse.pde.internal.core.ibundle.IBundleFragment;
import org.eclipse.pde.internal.core.ibundle.IBundlePlugin;
import org.eclipse.pde.internal.core.ibundle.IBundlePluginModelBase;
import org.eclipse.pde.internal.core.project.PDEProject;
import org.eclipse.pde.internal.core.text.bundle.ExportPackageHeader;
import org.eclipse.pde.internal.core.text.bundle.ImportPackageHeader;
import org.osgi.framework.Constants;

public class ProjectWrapper {
	private final IProject project;
	private IJavaProject javaProject;

	private String errorMessage;

	public static ProjectWrapper of(IWorkspace theWorkspace, String theName) {
		return new ProjectWrapper(theWorkspace.getRoot().getProject(theName));
	}

	public static ProjectWrapper of(IProject theProject) {
		return new ProjectWrapper(theProject);
	}

	private ProjectWrapper(IProject theProject) {
		project = Objects.requireNonNull(theProject);
	}

	public ProjectWrapper createProject() {
		if (!project.exists()) {
			try {
				project.create(null);
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

	public ProjectWrapper importProject(IWorkspace theWorkspace, IPath theProjectPath) {
		if (!project.exists()) {
			try {
				IProjectDescription aProjectDescription = theWorkspace.loadProjectDescription(theProjectPath.append(IProjectDescription.DESCRIPTION_FILE_NAME));

				project.create(aProjectDescription, null);
			}
			catch (CoreException theCause) {
				errorMessage = "Could not create project '" + project.getName() + " with project description': " + theCause.getMessage();
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

	public ProjectWrapper asJavaProject() {
		if (!hasError()) {
			if (hasNature(JavaCore.NATURE_ID)) {
				javaProject = JavaCore.create(project);
			}
			else {
				errorMessage = "Project '" + project.getName() + "' is no Java project";
			}
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

	public ProjectWrapper removeNature(String theNatureId) {
		if (!hasError()) {
			try {
				if (project.hasNature(theNatureId)) {
					IProjectDescription aProjectDescription = project.getDescription();
					List<String> allNatureIds = Arrays.asList(aProjectDescription.getNatureIds()).stream()
							.filter(theExistingNatureId -> !theExistingNatureId.equals(theNatureId))
							.collect(Collectors.toList());

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

	public boolean isExisting() {
		return project.exists();
	}

	public IProject getProject() {
		return project;
	}

	public IProjectDescription getProjectDescription() {
		try {
			return project.getDescription();
		}
		catch (CoreException theCause) {
			return null;
		}
	}

	public ProjectWrapper setProjectDescription(IProjectDescription theProjectDescription) {
		if (!hasError()) {
			try {
				project.setDescription(theProjectDescription, null);
			}
			catch (CoreException theCause) {
				errorMessage = "Could not set description for project '" + project.getName() + "': " + theCause.getMessage();
			}
		}
		return this;
	}

	public ProjectWrapper open() {
		try {
			if (!project.isOpen()) {
				project.open(null);
			}
		}
		catch (CoreException theCause) {
			errorMessage = "Could not open project '" + project.getName() + "': " + theCause.getMessage();
		}
		return this;
	}

	public boolean isOpen() {
		if (project.exists()) {
			return project.isOpen();
		}
		return false;
	}

	public ProjectWrapper close() {
		try {
			if (project.isOpen()) {
				project.close(null);
			}
		}
		catch (CoreException theCause) {
			errorMessage = "Could not close project '" + project.getName() + "': " + theCause.getMessage();
		}
		return this;
	}

	public boolean hasNature(String theNatureId) {
		try {
			if (project.exists()) {
				return project.hasNature(theNatureId);
			}
			return false;
		}
		catch (CoreException theCause) {
			throw new RuntimeException(theCause);
		}
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

		removeDefaultSourceFolder();
		if (!hasError()) {
			IPath aClasspath = project.getFullPath().append(theFolderName);

			addClasspathEntry(theProject -> JavaCore.newSourceEntry(aClasspath));
		}
		if (!hasError()) {
			try {
				IFolder aSourceFolder = project.getFolder(theFolderName);
				if (!aSourceFolder.exists()) {
					aSourceFolder.create(true, true, null);
				}
			}
			catch (CoreException theCause) {
				errorMessage = "Could not create source folder '" + theFolderName + "' in Java project '" + project.getName() + "': " + theCause.getMessage();
			}
		}
		return this;
	}

	public ProjectWrapper addLinkedSourceFolder(String theFolderName, IPath theSourceLocation) {
		if (!hasError()) {
			IPath aClasspath = project.getFullPath().append(theFolderName);

			addClasspathEntry(theProject -> JavaCore.newSourceEntry(aClasspath));
		}
		if (!hasError()) {
			IFolder aSourceLinkFolder = project.getFolder(theFolderName);

			try {
				aSourceLinkFolder.createLink(theSourceLocation, IResource.NONE, null);
			}
			catch (CoreException theCause) {
				errorMessage = "Could not create linked source folder '" + theFolderName + "' in Java project '" + project.getName() + "' to location '" + theSourceLocation + "': " + theCause.getMessage();
			}
		}
		return this;
	}

	public ProjectWrapper removeDefaultSourceFolder() {
		verifyJavaProject();

		if (!hasError()) {
			try {
				Arrays.asList(javaProject.getRawClasspath()).stream()
						.filter(theClasspathEntry -> theClasspathEntry.getPath().equals(project.getFullPath()))
						.findFirst()
						.ifPresent(theClasspathEntry -> removeClasspathEntry(theProject -> theClasspathEntry));
			}
			catch (JavaModelException theCause) {
				errorMessage = "Could not remove default source folder from Java project '" + project.getName() + "': " + theCause.getMessage();
			}
		}
		return this;
	}

	public ProjectWrapper removeSourceFolder(String theFolderName) {
		verifyJavaProject();

		if (!hasError()) {
			try {
				Arrays.asList(javaProject.getRawClasspath()).stream()
						.filter(theClasspathEntry -> theClasspathEntry.getPath().equals(project.getFullPath().append(theFolderName)))
						.findFirst()
						.ifPresent(theClasspathEntry -> removeClasspathEntry(theProject -> theClasspathEntry));
			}
			catch (JavaModelException theCause) {
				errorMessage = "Could not remove source folder '" + theFolderName + "' from Java project '" + project.getName() + "': " + theCause.getMessage();
			}
			if (!hasError()) {
				try {
					IFolder folder = project.getFolder(theFolderName);
					folder.delete(true, null);
				}
				catch (CoreException theCause) {
					errorMessage = "Could not create source folder '" + theFolderName + "' in Java project '" + project.getName() + "': " + theCause.getMessage();
				}
			}
		}
		return this;
	}

	public ProjectWrapper setOutputFolder(String theFolderName) {
		verifyJavaProject();

		if (!hasError()) {
			IFolder aBinaryFolder = project.getFolder(theFolderName);

			try {
				if (!aBinaryFolder.exists()) {
					aBinaryFolder.create(false, true, null);
				}
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
			if (!hasClasspathEntry(aClasspathEntry.getPath())) {
				try {
					List<IClasspathEntry> allClasspathEntries = new ArrayList<>(Arrays.asList(javaProject.getRawClasspath()));

					allClasspathEntries.add(aClasspathEntry);
					javaProject.setRawClasspath(allClasspathEntries.toArray(new IClasspathEntry[allClasspathEntries.size()]), null);
				}
				catch (JavaModelException theCause) {
					errorMessage = "Could not add classpath entry '" + aClasspathEntry + "' to Java project '" + project.getName() + "': " + theCause.getMessage();
				}
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
				errorMessage = "Could not remove classpath entry '" + aClasspathEntry + "' of Java project '" + project.getName() + "': " + theCause.getMessage();
			}
		}
		return this;
	}

	public ProjectWrapper removeClasspathEntry(IPath thePath) {
		verifyJavaProject();

		if (!hasError()) {
			try {
				List<IClasspathEntry> allClasspathEntries = new ArrayList<>(Arrays.asList(javaProject.getRawClasspath())).stream()
						.filter(theClasspathEntry -> !theClasspathEntry.getPath().equals(thePath))
						.collect(Collectors.toList());

				javaProject.setRawClasspath(allClasspathEntries.toArray(new IClasspathEntry[allClasspathEntries.size()]), null);
			}
			catch (JavaModelException theCause) {
				errorMessage = "Could not remove classpath entry with path '" + thePath + "' of Java project '" + project.getName() + "': " + theCause.getMessage();
			}
		}
		return this;
	}

	public boolean hasClasspathEntry(IPath thePath) {
		return getClasspathEntry(thePath) != null;
	}

	public IClasspathEntry getClasspathEntry(IPath thePath) {
		verifyJavaProject();

		try {
			return new ArrayList<>(Arrays.asList(javaProject.getRawClasspath())).stream()
					.filter(theClasspathEntry -> theClasspathEntry.getPath().equals(thePath))
					.findFirst().orElse(null);
		}
		catch (JavaModelException theCause) {
			throw new RuntimeException(theCause);
		}
	}

	@SuppressWarnings({
			"restriction",
			"deprecation" })
	public ProjectWrapper createPluginManifest(Supplier<Set<String>> theAdditionalPackageDependencies) {
		verifyJavaProject();

		if (!hasError()) {
			IPluginModel aBundlePluginModel = new WorkspaceBundlePluginModel(PDEProject.getManifest(project), PDEProject.getPluginXml(project));
			IBundlePluginModelBase aBundleModelBase = (IBundlePluginModelBase) aBundlePluginModel;
			IBundlePlugin aBundlePlugin = (IBundlePlugin) aBundlePluginModel.getPluginBase();

			try {
				IBundle aBundle = aBundleModelBase.getBundleModel().getBundle();

				aBundlePlugin.setSchemaVersion("1.0");
				aBundle.setHeader(Constants.BUNDLE_MANIFESTVERSION, "2");

				aBundlePlugin.setId(project.getName());
				aBundlePlugin.setVersion("0.0.0");
				aBundlePlugin.setProviderName("Reto Urfer (Profidata AG)");

				aBundle.setHeader(Constants.BUNDLE_REQUIREDEXECUTIONENVIRONMENT, "JavaSE-1.8");

				// Determine package dependencies from source code, exclude the ones starting with "java."
				Set<String> allSourcePackages = getSourcePackages();
				Set<String> allImportedPackages = getImportedPackages().stream()
						.filter(thePackage -> !thePackage.startsWith("java."))
						.collect(Collectors.toSet());

				allImportedPackages.addAll(Optional.ofNullable(theAdditionalPackageDependencies.get()).orElseGet(() -> Collections.emptySet()));
				allImportedPackages.removeAll(allSourcePackages);

				List<String> allSortedImportedPackages = new ArrayList<>(allImportedPackages);
				Collections.sort(allSortedImportedPackages);
				addtoImportPackageHeader(aBundle, allSortedImportedPackages);

				List<String> allSortedSourcePackages = new ArrayList<>(allSourcePackages);
				Collections.sort(allSortedSourcePackages);
				addtoExortPackageHeader(aBundle, allSortedSourcePackages);

				aBundleModelBase.save();
			}
			catch (CoreException theCause) {
				errorMessage = "Could not manifest for plugin project '" + project.getName() + "': " + theCause.getMessage();
			}
		}
		return this;
	}

	@SuppressWarnings("restriction")
	private void addtoImportPackageHeader(IBundle theBundle, List<String> theImportPackages) {
		ImportPackageHeader aImportPackageHeader = (ImportPackageHeader) theBundle.getManifestHeader(Constants.IMPORT_PACKAGE);
		List<String> someImportPackages = new ArrayList<>(theImportPackages);

		if (aImportPackageHeader == null) {
			theBundle.setHeader(Constants.IMPORT_PACKAGE, someImportPackages.get(0));

			someImportPackages.remove(0);
			aImportPackageHeader = (ImportPackageHeader) theBundle.getManifestHeader(Constants.IMPORT_PACKAGE);
		}

		for (String aPackage : someImportPackages) {
			aImportPackageHeader.addPackage(aPackage);
		}
	}

	@SuppressWarnings("restriction")
	private void addtoExortPackageHeader(IBundle theBundle, List<String> theImportPackages) {
		ExportPackageHeader aExportPackageHeader = (ExportPackageHeader) theBundle.getManifestHeader(Constants.EXPORT_PACKAGE);
		List<String> someExportPackages = new ArrayList<>(theImportPackages);

		if (aExportPackageHeader == null) {
			theBundle.setHeader(Constants.EXPORT_PACKAGE, someExportPackages.get(0));

			someExportPackages.remove(0);
			aExportPackageHeader = (ExportPackageHeader) theBundle.getManifestHeader(Constants.EXPORT_PACKAGE);
		}

		for (String aPackage : someExportPackages) {
			aExportPackageHeader.addPackage(aPackage);
		}
	}

	public ProjectWrapper addPackageDependenciesToPluginManifest(Supplier<Set<String>> theAdditionalPackageDependencies) {
		verifyJavaProject();

		if (!hasError()) {
			IPluginModel aBundlePluginModel = new WorkspaceBundlePluginModel(PDEProject.getManifest(project), PDEProject.getPluginXml(project));
			IBundlePluginModelBase aBundleModelBase = (IBundlePluginModelBase) aBundlePluginModel;
			IBundle aBundle = aBundleModelBase.getBundleModel().getBundle();
			Set<String> someAdditionalPackageDependencies = Optional.ofNullable(theAdditionalPackageDependencies.get()).orElseGet(() -> Collections.emptySet());

			if (!someAdditionalPackageDependencies.isEmpty()) {
				ImportPackageHeader aImportPackageHeader = (ImportPackageHeader) aBundle.getManifestHeader(Constants.IMPORT_PACKAGE);

				for (String aPackage : someAdditionalPackageDependencies) {
					aImportPackageHeader.addPackage(aPackage);
				}

				aBundleModelBase.save();
			}
		}
		return this;
	}

	@SuppressWarnings({
			"restriction",
			"deprecation" })
	public ProjectWrapper createTestFragmentManifest(IProject theHostBundleProject, Supplier<Set<String>> theAdditionalPackageDependencies) {
		verifyJavaProject();

		if (!hasError()) {
			IFragmentModel aFragmentModel = new WorkspaceBundleFragmentModel(PDEProject.getManifest(project), PDEProject.getFragmentXml(project));
			IBundlePluginModelBase aBundleModelBase = (IBundlePluginModelBase) aFragmentModel;
			IBundleFragment aBundleFragment = (IBundleFragment) aFragmentModel.getPluginBase();

			try {
				IBundle aBundle = aBundleModelBase.getBundleModel().getBundle();

				aBundleFragment.setSchemaVersion("1.0");
				aBundle.setHeader(Constants.BUNDLE_MANIFESTVERSION, "2");

				aBundleFragment.setName("Test wrapper fragment to " + theHostBundleProject.getName());
				aBundleFragment.setId(project.getName());
				aBundleFragment.setVersion("0.0.0");
				aBundleFragment.setProviderName("Reto Urfer (Profidata AG)");

				aBundleFragment.setPluginId(theHostBundleProject.getName());

				aBundle.setHeader(Constants.BUNDLE_REQUIREDEXECUTIONENVIRONMENT, "JavaSE-1.8");

				// Determine package dependencies from source code, exclude the ones starting with "java."
				Set<String> allImportedPackages = getImportedPackages().stream()
						.filter(thePackage -> !thePackage.startsWith("java."))
						.collect(Collectors.toSet());

				allImportedPackages.addAll(theAdditionalPackageDependencies.get());

				allImportedPackages.removeAll(getSourcePackages());

				List<String> allSortedImportedPackages = new ArrayList<>(allImportedPackages);

				Collections.sort(allSortedImportedPackages);
				addtoImportPackageHeader(aBundle, allSortedImportedPackages);

				aBundleModelBase.save();
			}
			catch (CoreException theCause) {
				errorMessage = "Could not manifest for test fragment project '" + project.getName() + "': " + theCause.getMessage();
			}
		}
		return this;
	}

	@SuppressWarnings("restriction")
	public ProjectWrapper createBuildProperties() {
		verifyJavaProject();

		if (!hasError()) {
			try {
				IFile aBuildPropertiesFile = PDEProject.getBuildProperties(project);
				WorkspaceBuildModel aBuildModel = new WorkspaceBuildModel(aBuildPropertiesFile);
				IBuildModelFactory aBuildModelFactory = aBuildModel.getFactory();
				IBuildEntry aBuildEntry;

				List<String> allSourceFolderNamess = Arrays.stream(javaProject.getRawClasspath())
						.filter(theEntry -> theEntry.getContentKind() == IPackageFragmentRoot.K_SOURCE && theEntry.getEntryKind() == IClasspathEntry.CPE_SOURCE)
						.map(theEntry -> theEntry.getPath().lastSegment())
						.collect(Collectors.toList());

				aBuildEntry = aBuildModelFactory.createEntry(IBuildEntry.JAR_PREFIX + ".");
				for (String aSourceFolderName : allSourceFolderNamess) {
					aBuildEntry.addToken(aSourceFolderName);
				}
				aBuildModel.getBuild().add(aBuildEntry);

				aBuildEntry = aBuildModelFactory.createEntry(IBuildEntry.BIN_INCLUDES);
				aBuildEntry.addToken("META-INF");
				aBuildEntry.addToken(".");
				aBuildModel.getBuild().add(aBuildEntry);

				((IEditableModel) aBuildModel).save();
			}
			catch (CoreException theCause) {
				errorMessage = "Could not build.properties file for bundle project '" + project.getName() + "': " + theCause.getMessage();
			}
		}
		return this;
	}

	public ProjectWrapper build() {
		if (!hasError()) {
			try {
				project.build(IncrementalProjectBuilder.CLEAN_BUILD, null);
			}
			catch (CoreException theCause) {
				errorMessage = "Could not build project '" + project.getName() + "': " + theCause.getMessage();
			}
		}
		return this;
	}

	public ProjectWrapper refresh() {
		if (!hasError()) {
			try {
				project.refreshLocal(IResource.DEPTH_INFINITE, null);
			}
			catch (CoreException theCause) {
				errorMessage = "Could not refresh project '" + project.getName() + "': " + theCause.getMessage();
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

	public Set<String> getSourcePackages() {
		Set<String> allSourcePackages = new HashSet<>();

		verifyJavaProject();

		if (!hasError()) {
			try {
				IPackageFragmentRoot[] allPackageFragmentRoots = javaProject.getAllPackageFragmentRoots();

				for (IPackageFragmentRoot aPackageFragmentRoot : allPackageFragmentRoots) {
					if (aPackageFragmentRoot.getKind() == IPackageFragmentRoot.K_SOURCE) {
						allSourcePackages.addAll(getSourcePackages(aPackageFragmentRoot));
					}
				}
			}
			catch (JavaModelException theCause) {
				errorMessage = "Could not extract source packages for project '" + project.getName() + theCause.getMessage();
			}
		}

		return allSourcePackages;
	}

	private Set<String> getSourcePackages(IJavaElement theJavaElement) throws JavaModelException {
		Set<String> allSourcePackages = new HashSet<>();

		if (theJavaElement instanceof IPackageFragment) {
			IPackageFragment aPackageFragment = (IPackageFragment) theJavaElement;

			Arrays.stream(aPackageFragment.getChildren())
					.filter(theChild -> theChild instanceof ICompilationUnit)
					.findAny().ifPresent(theCompilationUnit -> allSourcePackages.add(aPackageFragment.getElementName()));
		}

		if (theJavaElement instanceof IParent) {
			IParent aContainerElement = (IParent) theJavaElement;

			for (IJavaElement aJavaElement : aContainerElement.getChildren()) {
				allSourcePackages.addAll(getSourcePackages(aJavaElement));
			}
		}

		return allSourcePackages;
	}

	public Set<String> getImportedPackages() {
		Set<String> allImportedPackages = new HashSet<>();

		verifyJavaProject();

		if (!hasError()) {
			try {
				IPackageFragmentRoot[] allPackageFragmentRoots = javaProject.getAllPackageFragmentRoots();

				for (IPackageFragmentRoot aPackageFragmentRoot : allPackageFragmentRoots) {
					if (aPackageFragmentRoot.getKind() == IPackageFragmentRoot.K_SOURCE) {
						allImportedPackages.addAll(getImportedPackages(aPackageFragmentRoot));
					}
				}
			}
			catch (JavaModelException theCause) {
				errorMessage = "Could not extract imported packages for project '" + project.getName() + theCause.getMessage();
			}
		}

		return allImportedPackages;
	}

	private Set<String> getImportedPackages(IJavaElement theJavaElement) throws JavaModelException {
		Set<String> allImportedPackages = new HashSet<>();

		if (theJavaElement instanceof ICompilationUnit) {
			ICompilationUnit aCompilationUnit = (ICompilationUnit) theJavaElement;

			for (IImportDeclaration aImportDeclaration : aCompilationUnit.getImports()) {
				allImportedPackages.add(extractPackage(aImportDeclaration));
			}

		}
		else if (theJavaElement instanceof IParent) {
			IParent aContainerElement = (IParent) theJavaElement;

			for (IJavaElement aJavaElement : aContainerElement.getChildren()) {
				allImportedPackages.addAll(getImportedPackages(aJavaElement));
			}
		}

		return allImportedPackages;
	}

	private String extractPackage(IImportDeclaration theImportDeclaration) throws JavaModelException {
		final int skipLastElements = Flags.isStatic(theImportDeclaration.getFlags()) ? 2 : 1;

		List<String> allImportTokens = new ArrayList<>(Arrays.asList(theImportDeclaration.getElementName().split("\\.")));
		Collections.reverse(allImportTokens);

		List<String> aImportPackageTokens = allImportTokens.stream()
				.skip(skipLastElements)
				.filter(thePackageToken -> Character.isLowerCase(thePackageToken.codePointAt(0)))
				.collect(Collectors.toList());
		Collections.reverse(aImportPackageTokens);

		return aImportPackageTokens.stream().collect(Collectors.joining("."));
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
}
