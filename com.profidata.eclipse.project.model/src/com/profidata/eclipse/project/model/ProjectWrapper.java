package com.profidata.eclipse.project.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.osgi.framework.Constants;

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
import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.IClasspathAttribute;
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
import org.eclipse.pde.internal.core.ibundle.IManifestHeader;
import org.eclipse.pde.internal.core.project.PDEProject;
import org.eclipse.pde.internal.core.text.bundle.BundleSymbolicNameHeader;
import org.eclipse.pde.internal.core.text.bundle.ExportPackageHeader;
import org.eclipse.pde.internal.core.text.bundle.FragmentHostHeader;
import org.eclipse.pde.internal.core.text.bundle.ImportPackageHeader;

@SuppressWarnings("restriction")
public class ProjectWrapper {
	private final IProject project;
	private IJavaProject javaProject;

	private String errorMessage;
	private String protocolMessage;

	public static ProjectWrapper of(IWorkspace theWorkspace, String theName) {
		return new ProjectWrapper(theWorkspace.getRoot().getProject(theName));
	}

	public static ProjectWrapper of(IProject theProject) {
		return new ProjectWrapper(theProject);
	}

	private ProjectWrapper(IProject theProject) {
		this.project = Objects.requireNonNull(theProject);
	}

	public ProjectWrapper createProject() {
		if (!this.project.exists()) {
			try {
				this.project.create(null);
			}
			catch (CoreException theCause) {
				this.errorMessage = "Could not create project '" + this.project.getName() + "': " + theCause.getMessage();
			}
		}
		else {
			this.errorMessage = "Project '" + this.project.getName() + "' already exists";
		}
		return this;
	}

	public ProjectWrapper importProject(IWorkspace theWorkspace, IPath theProjectPath) {
		if (!this.project.exists()) {
			try {
				IProjectDescription aProjectDescription = theWorkspace.loadProjectDescription(theProjectPath.append(IProjectDescription.DESCRIPTION_FILE_NAME));

				this.project.create(aProjectDescription, null);
			}
			catch (CoreException theCause) {
				this.errorMessage = "Could not create project '" + this.project.getName() + " with project description': " + theCause.getMessage();
			}
		}
		else {
			this.errorMessage = "Project '" + this.project.getName() + "' already exists";
		}
		return this;
	}

	public ProjectWrapper toJavaProject() {
		if (!hasError()) {
			addNature(JavaCore.NATURE_ID);
			this.javaProject = JavaCore.create(this.project);
		}
		return this;
	}

	public ProjectWrapper asJavaProject() {
		if (!hasError()) {
			if (hasNature(JavaCore.NATURE_ID)) {
				this.javaProject = JavaCore.create(this.project);
			}
			else {
				this.errorMessage = "Project '" + this.project.getName() + "' is no Java project";
			}
		}
		return this;
	}

	public ProjectWrapper addNature(String theNatureId) {
		if (!hasError()) {
			try {
				if (!this.project.hasNature(theNatureId)) {
					IProjectDescription aProjectDescription = this.project.getDescription();
					List<String> allNatureIds = new ArrayList<>(Arrays.asList(aProjectDescription.getNatureIds()));

					allNatureIds.add(theNatureId);
					aProjectDescription.setNatureIds(allNatureIds.toArray(new String[allNatureIds.size()]));
					this.project.setDescription(aProjectDescription, null);
				}
			}
			catch (CoreException theCause) {
				this.errorMessage = "Could not add nature '" + theNatureId + "' to project '" + this.project.getName() + "': " + theCause.getMessage();
			}
		}
		return this;
	}

	public ProjectWrapper removeNature(String theNatureId) {
		if (!hasError()) {
			try {
				if (this.project.hasNature(theNatureId)) {
					IProjectDescription aProjectDescription = this.project.getDescription();
					List<String> allNatureIds = Arrays.asList(aProjectDescription.getNatureIds()).stream()
							.filter(theExistingNatureId -> !theExistingNatureId.equals(theNatureId))
							.collect(Collectors.toList());

					aProjectDescription.setNatureIds(allNatureIds.toArray(new String[allNatureIds.size()]));
					this.project.setDescription(aProjectDescription, null);
				}
			}
			catch (CoreException theCause) {
				this.errorMessage = "Could not add nature '" + theNatureId + "' to project '" + this.project.getName() + "': " + theCause.getMessage();
			}
		}
		return this;
	}

	public boolean isExisting() {
		return this.project.exists();
	}

	public IProject getProject() {
		return this.project;
	}

	public IJavaProject getJavaProject() {
		return this.javaProject;
	}

	public IProjectDescription getProjectDescription() {
		try {
			return this.project.getDescription();
		}
		catch (CoreException theCause) {
			return null;
		}
	}

	public ProjectWrapper setProjectDescription(IProjectDescription theProjectDescription) {
		if (!hasError()) {
			try {
				this.project.setDescription(theProjectDescription, null);
			}
			catch (CoreException theCause) {
				this.errorMessage = "Could not set description for project '" + this.project.getName() + "': " + theCause.getMessage();
			}
		}
		return this;
	}

	public ProjectWrapper open() {
		try {
			if (!this.project.isOpen()) {
				this.project.open(null);
			}
		}
		catch (CoreException theCause) {
			this.errorMessage = "Could not open project '" + this.project.getName() + "': " + theCause.getMessage();
		}
		return this;
	}

	public boolean isOpen() {
		if (this.project.exists()) {
			return this.project.isOpen();
		}
		return false;
	}

	public ProjectWrapper close() {
		try {
			if (this.project.isOpen()) {
				this.project.close(null);
			}
		}
		catch (CoreException theCause) {
			this.errorMessage = "Could not close project '" + this.project.getName() + "': " + theCause.getMessage();
		}
		return this;
	}

	public boolean hasNature(String theNatureId) {
		try {
			if (this.project.exists()) {
				return this.project.hasNature(theNatureId);
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
				ICommand aBuilderCommand = this.project.getDescription().newCommand();
				aBuilderCommand.setBuilderName(theBuilderId);
			}
			catch (CoreException theCause) {
				this.errorMessage = "Could not add builder '" + theBuilderId + "' to project '" + this.project.getName() + "': " + theCause.getMessage();
			}
		}
		return this;
	}

	public ProjectWrapper addSourceFolder(String theFolderName) {
		verifyJavaProject();

		removeDefaultSourceFolder();
		if (!hasError()) {
			IPath aClasspath = this.project.getFullPath().append(theFolderName);

			addClasspathEntry(theProject -> JavaCore.newSourceEntry(aClasspath));
		}
		if (!hasError()) {
			try {
				IFolder aSourceFolder = this.project.getFolder(theFolderName);
				if (!aSourceFolder.exists()) {
					aSourceFolder.create(true, true, null);
				}
			}
			catch (CoreException theCause) {
				this.errorMessage = "Could not create source folder '" + theFolderName + "' in Java project '" + this.project.getName() + "': " + theCause.getMessage();
			}
		}
		return this;
	}

	public ProjectWrapper addLinkedSourceFolder(String theFolderName, IPath theSourceLocation) {
		if (!hasError()) {
			IPath aClasspath = this.project.getFullPath().append(theFolderName);

			addClasspathEntry(theProject -> JavaCore.newSourceEntry(aClasspath));
		}
		if (!hasError()) {
			IFolder aSourceLinkFolder = this.project.getFolder(theFolderName);

			try {
				aSourceLinkFolder.createLink(theSourceLocation, IResource.NONE, null);
			}
			catch (CoreException theCause) {
				this.errorMessage = "Could not create linked source folder '" + theFolderName + "' in Java project '" + this.project.getName() + "' to location '" + theSourceLocation + "': " + theCause.getMessage();
			}
		}
		return this;
	}

	public ProjectWrapper removeDefaultSourceFolder() {
		verifyJavaProject();

		if (!hasError()) {
			try {
				Arrays.asList(this.javaProject.getRawClasspath()).stream()
						.filter(theClasspathEntry -> theClasspathEntry.getPath().equals(this.project.getFullPath()))
						.findFirst()
						.ifPresent(theClasspathEntry -> removeClasspathEntry(theProject -> theClasspathEntry));
			}
			catch (JavaModelException theCause) {
				this.errorMessage = "Could not remove default source folder from Java project '" + this.project.getName() + "': " + theCause.getMessage();
			}
		}
		return this;
	}

	public ProjectWrapper removeSourceFolder(String theFolderName) {
		verifyJavaProject();

		if (!hasError()) {
			try {
				Arrays.asList(this.javaProject.getRawClasspath()).stream()
						.filter(theClasspathEntry -> theClasspathEntry.getPath().equals(this.project.getFullPath().append(theFolderName)))
						.findFirst()
						.ifPresent(theClasspathEntry -> removeClasspathEntry(theProject -> theClasspathEntry));
			}
			catch (JavaModelException theCause) {
				this.errorMessage = "Could not remove source folder '" + theFolderName + "' from Java project '" + this.project.getName() + "': " + theCause.getMessage();
			}
			if (!hasError()) {
				try {
					IFolder folder = this.project.getFolder(theFolderName);
					folder.delete(true, null);
				}
				catch (CoreException theCause) {
					this.errorMessage = "Could not create source folder '" + theFolderName + "' in Java project '" + this.project.getName() + "': " + theCause.getMessage();
				}
			}
		}
		return this;
	}

	public ProjectWrapper setOutputFolder(String theFolderName) {
		verifyJavaProject();

		if (!hasError()) {
			IFolder aBinaryFolder = this.project.getFolder(theFolderName);

			try {
				if (!aBinaryFolder.exists()) {
					aBinaryFolder.create(false, true, null);
				}
				this.javaProject.setOutputLocation(aBinaryFolder.getFullPath(), null);
			}
			catch (CoreException theCause) {
				this.errorMessage = "Could not set output folder '" + theFolderName + "' in Java project '" + this.project.getName() + "': " + theCause.getMessage();
			}
		}
		return this;
	}

	public ProjectWrapper addClasspathEntry(Function<IProject, IClasspathEntry> theClasspathEntrySupplier) {
		verifyJavaProject();

		if (!hasError()) {
			IClasspathEntry aClasspathEntry = theClasspathEntrySupplier.apply(this.project);
			if (!hasClasspathEntry(aClasspathEntry.getPath())) {
				try {
					List<IClasspathEntry> allClasspathEntries = new ArrayList<>(Arrays.asList(this.javaProject.getRawClasspath()));

					allClasspathEntries.add(aClasspathEntry);
					this.javaProject.setRawClasspath(allClasspathEntries.toArray(new IClasspathEntry[allClasspathEntries.size()]), null);
				}
				catch (JavaModelException theCause) {
					this.errorMessage = "Could not add classpath entry '" + aClasspathEntry + "' to Java project '" + this.project.getName() + "': " + theCause.getMessage();
				}
			}

			else if (hasDifferentAccessRules(aClasspathEntry.getAccessRules(), getClasspathEntry(aClasspathEntry.getPath()).getAccessRules())) {
				try {
					List<IClasspathEntry> allClasspathEntries = new ArrayList<>(Arrays.asList(this.javaProject.getRawClasspath())).stream()
							.filter(theClasspathEntry -> !theClasspathEntry.getPath().equals(aClasspathEntry.getPath()))
							.collect(Collectors.toList());

					allClasspathEntries.add(aClasspathEntry);
					this.javaProject.setRawClasspath(allClasspathEntries.toArray(new IClasspathEntry[allClasspathEntries.size()]), null);
				}
				catch (JavaModelException theCause) {
					this.errorMessage = "Could not modify accessrules of classpath entry '" + aClasspathEntry + "' in Java project '" + this.project.getName() + "': " + theCause.getMessage();
				}
			}
		}
		return this;
	}

	private boolean hasDifferentAccessRules(IAccessRule[] theRequestedAccessRules, IAccessRule[] theExistingAccessRules) {
		if (theRequestedAccessRules.length != theExistingAccessRules.length) {
			return true;
		}

		REQUESTED_ACCESS_RULE_LOOP: for (IAccessRule aRequestedAccessRule : theRequestedAccessRules) {
			for (IAccessRule aExistingAccessRule : theExistingAccessRules) {
				if (aRequestedAccessRule.getPattern().equals(aExistingAccessRule.getPattern()) && aRequestedAccessRule.getKind() == aExistingAccessRule.getKind()) {
					continue REQUESTED_ACCESS_RULE_LOOP;
				}
			}
			return true;
		}

		return false;
	}

	public ProjectWrapper removeClasspathEntry(Function<IProject, IClasspathEntry> theClasspathEntrySupplier) {
		verifyJavaProject();

		if (!hasError()) {
			IClasspathEntry aClasspathEntry = theClasspathEntrySupplier.apply(this.project);
			try {
				List<IClasspathEntry> allClasspathEntries = new ArrayList<>(Arrays.asList(this.javaProject.getRawClasspath())).stream()
						.filter(theClasspathEntry -> !theClasspathEntry.getPath().equals(aClasspathEntry.getPath()))
						.collect(Collectors.toList());

				this.javaProject.setRawClasspath(allClasspathEntries.toArray(new IClasspathEntry[allClasspathEntries.size()]), null);
			}
			catch (JavaModelException theCause) {
				this.errorMessage = "Could not remove classpath entry '" + aClasspathEntry + "' of Java project '" + this.project.getName() + "': " + theCause.getMessage();
			}
		}
		return this;
	}

	public ProjectWrapper removeClasspathEntry(IPath thePath) {
		verifyJavaProject();

		if (!hasError()) {
			try {
				List<IClasspathEntry> allClasspathEntries = new ArrayList<>(Arrays.asList(this.javaProject.getRawClasspath())).stream()
						.filter(theClasspathEntry -> !theClasspathEntry.getPath().equals(thePath))
						.collect(Collectors.toList());

				this.javaProject.setRawClasspath(allClasspathEntries.toArray(new IClasspathEntry[allClasspathEntries.size()]), null);
			}
			catch (JavaModelException theCause) {
				this.errorMessage = "Could not remove classpath entry with path '" + thePath + "' of Java project '" + this.project.getName() + "': " + theCause.getMessage();
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
			if (!hasError()) {
				return new ArrayList<>(Arrays.asList(this.javaProject.getRawClasspath())).stream()
						.filter(theClasspathEntry -> theClasspathEntry.getPath().equals(thePath))
						.findFirst().orElse(null);
			}
			return null;
		}
		catch (JavaModelException theCause) {
			throw new RuntimeException(theCause);
		}
	}

	@SuppressWarnings({
			"deprecation" })
	public ProjectWrapper createPluginManifest(String theExecutionEnvironment, Supplier<Set<String>> theAdditionalPackageDependencies) {
		verifyJavaProject();

		if (!hasError()) {
			IPluginModel aBundlePluginModel = new WorkspaceBundlePluginModel(PDEProject.getManifest(this.project), PDEProject.getPluginXml(this.project));
			IBundlePluginModelBase aBundleModelBase = (IBundlePluginModelBase) aBundlePluginModel;
			IBundlePlugin aBundlePlugin = (IBundlePlugin) aBundlePluginModel.getPluginBase();

			try {
				IBundle aBundle = aBundleModelBase.getBundleModel().getBundle();

				aBundlePlugin.setSchemaVersion("1.0");
				aBundle.setHeader(Constants.BUNDLE_MANIFESTVERSION, "2");

				aBundlePlugin.setId(this.project.getName());
				aBundlePlugin.setVersion("0.0.0");
				aBundlePlugin.setProviderName("Reto Urfer (Profidata AG)");

				aBundle.setHeader(Constants.BUNDLE_REQUIREDEXECUTIONENVIRONMENT, theExecutionEnvironment);

				// Determine package dependencies from source code, exclude the ones starting with "java."
				Set<String> allSourcePackages = getSourcePackages();
				Set<String> allImportedPackages = getImportedPackages().stream()
						.filter(thePackage -> !thePackage.startsWith("java."))
						.collect(Collectors.toSet());

				allImportedPackages.addAll(Optional.ofNullable(theAdditionalPackageDependencies.get()).orElseGet(() -> Collections.emptySet()));
				allImportedPackages.removeAll(allSourcePackages);

				List<String> allSortedImportedPackages = new ArrayList<>(allImportedPackages);
				Collections.sort(allSortedImportedPackages);
				addToImportPackageHeader(aBundle, allSortedImportedPackages);

				List<String> allSortedSourcePackages = new ArrayList<>(allSourcePackages);
				Collections.sort(allSortedSourcePackages);
				addToExortPackageHeader(aBundle, allSortedSourcePackages);

				aBundleModelBase.save();
			}
			catch (CoreException theCause) {
				this.errorMessage = "Could not manifest for plugin project '" + this.project.getName() + "': " + theCause.getMessage();
			}
		}
		return this;
	}

	public ProjectWrapper setSingletonPlugin(boolean theSingleton) {
		verifyJavaProject();

		if (!hasError()) {
			IPluginModel aBundlePluginModel = new WorkspaceBundlePluginModel(PDEProject.getManifest(this.project), PDEProject.getPluginXml(this.project));
			IBundlePluginModelBase aBundleModelBase = (IBundlePluginModelBase) aBundlePluginModel;
			IBundle aBundle = aBundleModelBase.getBundleModel().getBundle();
			IManifestHeader aHeader = aBundle.getManifestHeader(Constants.BUNDLE_SYMBOLICNAME);

			if (aHeader instanceof BundleSymbolicNameHeader) {
				BundleSymbolicNameHeader aBundleSymbolicNameHeader = (BundleSymbolicNameHeader) aHeader;

				if (aBundleSymbolicNameHeader.isSingleton() != theSingleton) {
					aBundleSymbolicNameHeader.setSingleton(theSingleton);
					addProtocolMessage(" - " + this.project.getName() + " -> singleton = " + theSingleton);
					aBundleModelBase.save();
				}
			}

			else {
				this.errorMessage = "bundle symbolic name header of project '" + this.project.getName() + "' is not of type BundleSymbolicNameHeader";
			}

		}
		return this;
	}

	public boolean isFragment() {
		IPluginModel aBundlePluginModel = new WorkspaceBundlePluginModel(PDEProject.getManifest(this.project), PDEProject.getPluginXml(this.project));
		IBundlePluginModelBase aBundleModelBase = (IBundlePluginModelBase) aBundlePluginModel;
		IBundle aBundle = aBundleModelBase.getBundleModel().getBundle();
		IManifestHeader aHeader = aBundle.getManifestHeader(Constants.FRAGMENT_HOST);

		return (aHeader instanceof FragmentHostHeader);
	}

	public String getFragmentHostId() {
		IPluginModel aBundlePluginModel = new WorkspaceBundlePluginModel(PDEProject.getManifest(this.project), PDEProject.getPluginXml(this.project));
		IBundlePluginModelBase aBundleModelBase = (IBundlePluginModelBase) aBundlePluginModel;
		IBundle aBundle = aBundleModelBase.getBundleModel().getBundle();
		IManifestHeader aHeader = aBundle.getManifestHeader(Constants.FRAGMENT_HOST);

		if (aHeader instanceof FragmentHostHeader) {
			return ((FragmentHostHeader) aHeader).getHostId();
		}
		return null;
	}

	private boolean addToImportPackageHeader(IBundle theBundle, List<String> theImportPackages) {
		ImportPackageHeader aImportPackageHeader = (ImportPackageHeader) theBundle.getManifestHeader(Constants.IMPORT_PACKAGE);
		List<String> someImportPackages = new ArrayList<>(theImportPackages);
		int somePackagesAdded = 0;

		if (aImportPackageHeader == null) {
			String aPackage = someImportPackages.get(0);
			theBundle.setHeader(Constants.IMPORT_PACKAGE, aPackage);

			someImportPackages.remove(0);
			aImportPackageHeader = (ImportPackageHeader) theBundle.getManifestHeader(Constants.IMPORT_PACKAGE);
			somePackagesAdded++;

			addProtocolMessage(" - " + aPackage);
		}

		for (String aPackage : someImportPackages) {
			String aPackageOnly = aPackage;
			int aFirstDirectiveDelimiterIndex = aPackage.indexOf(";");

			if (aFirstDirectiveDelimiterIndex > 0) {
				aPackageOnly = aPackageOnly.substring(0, aFirstDirectiveDelimiterIndex);
			}

			if (!aImportPackageHeader.hasPackage(aPackageOnly)) {
				aImportPackageHeader.addPackage(aPackage);
				somePackagesAdded++;

				addProtocolMessage(" - " + aPackage);
			}
		}

		return somePackagesAdded > 0;
	}

	private boolean addToExortPackageHeader(IBundle theBundle, List<String> theImportPackages) {
		ExportPackageHeader aExportPackageHeader = (ExportPackageHeader) theBundle.getManifestHeader(Constants.EXPORT_PACKAGE);
		List<String> someExportPackages = new ArrayList<>(theImportPackages);
		int somePackagesAdded = 0;

		if (aExportPackageHeader == null) {
			theBundle.setHeader(Constants.EXPORT_PACKAGE, someExportPackages.get(0));

			someExportPackages.remove(0);
			aExportPackageHeader = (ExportPackageHeader) theBundle.getManifestHeader(Constants.EXPORT_PACKAGE);
			somePackagesAdded++;
		}

		for (String aPackage : someExportPackages) {
			if (!aExportPackageHeader.hasPackage(aPackage)) {
				aExportPackageHeader.addPackage(aPackage);
				somePackagesAdded++;
			}
		}

		return somePackagesAdded > 0;
	}

	public ProjectWrapper addPackageDependenciesToPluginManifest(Supplier<Set<String>> theAdditionalPackageDependencies) {
		verifyJavaProject();

		if (!hasError()) {
			IPluginModel aBundlePluginModel = new WorkspaceBundlePluginModel(PDEProject.getManifest(this.project), PDEProject.getPluginXml(this.project));
			IBundlePluginModelBase aBundleModelBase = (IBundlePluginModelBase) aBundlePluginModel;
			IBundle aBundle = aBundleModelBase.getBundleModel().getBundle();
			Set<String> someAdditionalPackageDependencies = Optional.ofNullable(theAdditionalPackageDependencies.get()).orElseGet(() -> Collections.emptySet());

			if (!someAdditionalPackageDependencies.isEmpty()) {
				if (addToImportPackageHeader(aBundle, new ArrayList<>(someAdditionalPackageDependencies))) {
					aBundleModelBase.save();
				}
			}
		}
		return this;
	}

	public ProjectWrapper addProjectDependenciesToProject(Supplier<Set<String>> theAdditionalProjectDependencies) {
		final IAccessRule[] NO_ACCESS_RULES = {};
		final IClasspathAttribute[] NO_EXTRA_ATTRIBUTES = {};
		Set<String> someAdditionalProjectDependencies = Optional.ofNullable(theAdditionalProjectDependencies.get()).orElseGet(() -> Collections.emptySet());

		if (!someAdditionalProjectDependencies.isEmpty()) {
			someAdditionalProjectDependencies.forEach(
					theProjectDependency -> addClasspathEntry(
							theProject -> JavaCore.newProjectEntry(
									org.eclipse.core.runtime.Path.fromPortableString("/" + theProjectDependency),
									NO_ACCESS_RULES,
									false,
									NO_EXTRA_ATTRIBUTES,
									false)));
		}
		return this;
	}

	@SuppressWarnings({
			"deprecation" })
	public ProjectWrapper createTestFragmentManifest(
			IProject theHostBundleProject,
			String theExecutionEnvironment,
			Supplier<Set<String>> theAdditionalPackageDependencies,
			Supplier<Set<String>> theIgnorePackageDependencies,
			Map<String, String> theSpecialPackageDependencies) {
		verifyJavaProject();

		if (!hasError()) {
			IFragmentModel aFragmentModel = new WorkspaceBundleFragmentModel(PDEProject.getManifest(this.project), PDEProject.getFragmentXml(this.project));
			IBundlePluginModelBase aBundleModelBase = (IBundlePluginModelBase) aFragmentModel;
			IBundleFragment aBundleFragment = (IBundleFragment) aFragmentModel.getPluginBase();

			try {
				IBundle aBundle = aBundleModelBase.getBundleModel().getBundle();
				ProjectWrapper aHostBundleProjectWrapper = ProjectWrapper.of(theHostBundleProject);

				aBundleFragment.setSchemaVersion("1.0");
				aBundle.setHeader(Constants.BUNDLE_MANIFESTVERSION, "2");

				aBundleFragment.setName("Test wrapper fragment to " + theHostBundleProject.getName());
				aBundleFragment.setId(this.project.getName());
				aBundleFragment.setVersion("0.0.0");
				aBundleFragment.setProviderName("Reto Urfer (Profidata AG)");

				aBundleFragment.setPluginId(theHostBundleProject.getName());

				aBundle.setHeader(Constants.BUNDLE_REQUIREDEXECUTIONENVIRONMENT, theExecutionEnvironment);

				List<String> allSortedImportedPackages = determinePackagesToImport(
						aHostBundleProjectWrapper.getSourcePackages(),
						theAdditionalPackageDependencies,
						theIgnorePackageDependencies,
						theSpecialPackageDependencies);
				addToImportPackageHeader(aBundle, allSortedImportedPackages);

				aBundleModelBase.save();
			}
			catch (CoreException theCause) {
				this.errorMessage = "Could not manifest for test fragment project '" + this.project.getName() + "': " + theCause.getMessage();
			}
		}
		return this;
	}

	@SuppressWarnings({})
	public ProjectWrapper createTestFragmentPackageDependencies(
			IWorkspace theWorkspace,
			Supplier<Set<String>> theAdditionalPackageDependencies,
			Supplier<Set<String>> theIgnorePackageDependencies,
			Map<String, String> theSpecialPackageDependencies) {
		verifyJavaProject();

		if (!hasError()) {
			IFragmentModel aFragmentModel = new WorkspaceBundleFragmentModel(PDEProject.getManifest(this.project), PDEProject.getFragmentXml(this.project));
			IBundlePluginModelBase aBundleModelBase = (IBundlePluginModelBase) aFragmentModel;
			IBundle aBundle = aBundleModelBase.getBundleModel().getBundle();
			ProjectWrapper aHostBundleProjectWrapper = ProjectWrapper.of(theWorkspace, getFragmentHostId()).asJavaProject();

			List<String> allSortedImportedPackages = determinePackagesToImport(
					aHostBundleProjectWrapper.getSourcePackages(),
					theAdditionalPackageDependencies,
					theIgnorePackageDependencies,
					theSpecialPackageDependencies);
			if (addToImportPackageHeader(aBundle, allSortedImportedPackages)) {
				aBundleModelBase.save();
			}
		}
		return this;
	}

	private List<String> determinePackagesToImport(
			Set<String> theSourcePackages,
			Supplier<Set<String>> theAdditionalPackageDependencies,
			Supplier<Set<String>> theIgnorePackageDependencies,
			Map<String, String> theSpecialPackageDependencies) {
		// Determine package dependencies from source code, exclude the ones starting with "java."
		Set<String> allImportedPackages = getImportedPackages().stream()
				.filter(thePackage -> !thePackage.startsWith("java."))
				.collect(Collectors.toSet());
		Set<String> someAdditionalPackageDependencies = theAdditionalPackageDependencies.get();

		// remove all the packages which are also defined in the additional packages because there the may have additional attributes defined
		allImportedPackages.removeAll(
				someAdditionalPackageDependencies.stream()
						.map(thePackageDependency -> thePackageDependency.split(";", 2)[0])
						.collect(Collectors.toSet()));

		// add all the additional packages defined with there attributes
		allImportedPackages.addAll(someAdditionalPackageDependencies);

		// remove all the packages defined in the host bundle
		allImportedPackages.removeAll(theSourcePackages);

		// remove all the packages defined in this test fragment
		allImportedPackages.removeAll(getSourcePackages());

		// remove all the packages defined as to be ignored
		Set<String> someIgnoredPackages = new HashSet<>();
		for (String aPackagePrefix : theIgnorePackageDependencies.get()) {
			someIgnoredPackages.addAll(
					allImportedPackages.stream()
							.filter(thePackage -> thePackage.startsWith(aPackagePrefix))
							.collect(Collectors.toSet()));
		}
		allImportedPackages.removeAll(someIgnoredPackages);

		allImportedPackages = replaceSpecialPackageDependencies(allImportedPackages, theSpecialPackageDependencies);

		List<String> allSortedImportedPackages = new ArrayList<>(allImportedPackages);
		Collections.sort(allSortedImportedPackages);

		return allSortedImportedPackages;
	}

	private Set<String> replaceSpecialPackageDependencies(Set<String> theImportedPackages, Map<String, String> theSpecialDependencies) {
		return theImportedPackages.stream()
				.map(thePackage -> (theSpecialDependencies.containsKey(thePackage)) ? theSpecialDependencies.get(thePackage) : thePackage)
				.collect(Collectors.toSet());
	}

	public ProjectWrapper createBuildProperties() {
		verifyJavaProject();

		if (!hasError()) {
			try {
				IFile aBuildPropertiesFile = PDEProject.getBuildProperties(this.project);
				WorkspaceBuildModel aBuildModel = new WorkspaceBuildModel(aBuildPropertiesFile);
				IBuildModelFactory aBuildModelFactory = aBuildModel.getFactory();
				IBuildEntry aBuildEntry;

				List<String> allSourceFolderNamess = Arrays.stream(this.javaProject.getRawClasspath())
						.filter(theEntry -> theEntry.getContentKind() == IPackageFragmentRoot.K_SOURCE && theEntry.getEntryKind() == IClasspathEntry.CPE_SOURCE)
						.map(theEntry -> theEntry.getPath().lastSegment())
						.collect(Collectors.toList());

				aBuildEntry = aBuildModelFactory.createEntry(IBuildEntry.JAR_PREFIX + ".");
				for (String aSourceFolderName : allSourceFolderNamess) {
					aBuildEntry.addToken(aSourceFolderName);
				}
				aBuildModel.getBuild().add(aBuildEntry);

				aBuildEntry = aBuildModelFactory.createEntry(IBuildEntry.BIN_INCLUDES);
				aBuildEntry.addToken("META-INF/");
				aBuildEntry.addToken(".");
				aBuildModel.getBuild().add(aBuildEntry);

				((IEditableModel) aBuildModel).save();
			}
			catch (CoreException theCause) {
				this.errorMessage = "Could not build.properties file for bundle project '" + this.project.getName() + "': " + theCause.getMessage();
			}
		}
		return this;
	}

	public ProjectWrapper build() {
		if (!hasError()) {
			try {
				this.project.build(IncrementalProjectBuilder.CLEAN_BUILD, null);
			}
			catch (CoreException theCause) {
				this.errorMessage = "Could not build project '" + this.project.getName() + "': " + theCause.getMessage();
			}
		}
		return this;
	}

	public ProjectWrapper refresh() {
		if (!hasError()) {
			try {
				this.project.refreshLocal(IResource.DEPTH_INFINITE, null);
			}
			catch (CoreException theCause) {
				this.errorMessage = "Could not refresh project '" + this.project.getName() + "': " + theCause.getMessage();
			}
		}
		return this;
	}

	public ProjectWrapper createPackage(String theFolder, String thePackage) {
		verifyJavaProject();

		if (!hasError()) {
			IPackageFragmentRoot aSourceFolder = this.javaProject.getPackageFragmentRoot(this.project.getFolder(theFolder));
			try {
				aSourceFolder.createPackageFragment(thePackage, true, null);
			}
			catch (JavaModelException theCause) {
				this.errorMessage = "Could not create package '" + thePackage + "' in source folder '" + theFolder + "': " + theCause.getMessage();
			}
		}
		return this;
	}

	public Set<String> getSourcePackages() {
		Set<String> allSourcePackages = new HashSet<>();

		verifyJavaProject();

		if (!hasError()) {
			try {
				IPackageFragmentRoot[] allPackageFragmentRoots = this.javaProject.getAllPackageFragmentRoots();

				for (IPackageFragmentRoot aPackageFragmentRoot : allPackageFragmentRoots) {
					if (aPackageFragmentRoot.getKind() == IPackageFragmentRoot.K_SOURCE && aPackageFragmentRoot.getParent().getElementName().equals(this.project.getName())) {
						allSourcePackages.addAll(getSourcePackages(aPackageFragmentRoot));
					}
				}
			}
			catch (JavaModelException theCause) {
				this.errorMessage = "Could not extract source packages for project '" + this.project.getName() + theCause.getMessage();
			}
		}

		return allSourcePackages;
	}

	private Set<String> getSourcePackages(IPackageFragmentRoot thePackageFragmentRoot) throws JavaModelException {
		Set<String> allSourcePackages = new HashSet<>();

		for (IJavaElement aJavaElement : thePackageFragmentRoot.getChildren()) {
			if (aJavaElement instanceof IPackageFragment) {
				IPackageFragment aPackageFragment = (IPackageFragment) aJavaElement;

				Arrays.stream(aPackageFragment.getChildren())
						.filter(theChild -> theChild instanceof ICompilationUnit)
						.findAny().ifPresent(theCompilationUnit -> allSourcePackages.add(aPackageFragment.getElementName()));
			}
		}

		return allSourcePackages;
	}

	public Set<String> getImportedPackages() {
		Set<String> allImportedPackages = new HashSet<>();

		verifyJavaProject();

		if (!hasError()) {
			try {
				IPackageFragmentRoot[] allPackageFragmentRoots = this.javaProject.getAllPackageFragmentRoots();

				for (IPackageFragmentRoot aPackageFragmentRoot : allPackageFragmentRoots) {
					if (aPackageFragmentRoot.getKind() == IPackageFragmentRoot.K_SOURCE && aPackageFragmentRoot.getParent().getElementName().equals(this.project.getName())) {
						allImportedPackages.addAll(getImportedPackages(aPackageFragmentRoot));
					}
				}
			}
			catch (JavaModelException theCause) {
				this.errorMessage = "Could not extract imported packages for project '" + this.project.getName() + theCause.getMessage();
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
		if (!hasError() && this.javaProject == null) {
			this.errorMessage = "project '" + this.project.getName() + "' is not a Java project";
		}
	}

	public boolean hasError() {
		return this.errorMessage != null;
	}

	public String getErrorMessage() {
		return this.errorMessage;
	}

	private void addProtocolMessage(String theMessage) {
		if (this.protocolMessage == null) {
			this.protocolMessage = theMessage;
		}
		else {
			this.protocolMessage += "\n" + theMessage;
		}
	}

	public boolean hasProtocol() {
		return this.protocolMessage != null;
	}

	public String getProtocolMessage() {
		return this.protocolMessage;
	}
}
