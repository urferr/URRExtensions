package com.profidata.xentis.scripts;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.pde.internal.core.feature.WorkspaceFeatureModel;
import org.eclipse.pde.internal.core.ifeature.IFeature;
import org.eclipse.pde.internal.core.ifeature.IFeatureChild;
import org.eclipse.pde.internal.core.ifeature.IFeatureModel;
import org.eclipse.pde.internal.core.ifeature.IFeaturePlugin;
import org.eclipse.pde.internal.core.iproduct.IProduct;
import org.eclipse.pde.internal.core.iproduct.IProductFeature;
import org.eclipse.pde.internal.core.iproduct.IProductModel;
import org.eclipse.pde.internal.core.product.WorkspaceProductModel;
import org.eclipse.pde.internal.core.project.PDEProject;

import com.profidata.eclipse.project.model.ProjectConstants;
import com.profidata.eclipse.project.model.ProjectWrapper;
import com.profidata.xentis.config.ImportConfiguration;
import com.profidata.xentis.config.ImportFeatureProject;
import com.profidata.xentis.config.PackageDependencyConfiguration;
import com.profidata.xentis.config.ProjectDependencyConfiguration;
import com.profidata.xentis.config.URRImportConfiguration;
import com.profidata.xentis.config.XCImportConfiguration;
import com.profidata.xentis.fix.IgnoreProjectFolder;
import com.profidata.xentis.fix.RemoveGradleNatureFromPlugins;

public class XentisWorkspace {
	private static PrintStream output;
	private static PrintStream error;

	public static void initialize(InputStream theInput, PrintStream theOutput, PrintStream theError) {
		XentisWorkspace.output = theOutput;
		XentisWorkspace.error = theError;
	}

	public static void main(String[] args) throws Exception {
		new XentisWorkspace().execute();
	}

	private void execute() {
		IWorkspace aWorkspace = ResourcesPlugin.getWorkspace();
		boolean aAutoBuildWasEnabled = disableAutoBuild(aWorkspace);

		try {
			output.println("Fix projects with Plugin/Gradle nature");
			output.println("======================================");
			RemoveGradleNatureFromPlugins.run(output, error);

			output.println("");
			output.println("Exchange Gradle with Plugin nature");
			output.println("==================================");
			convertProjectFromGradleToPlugin(aWorkspace, "com.profidata.xentis.test");

			output.println("");
			output.println("Fix specific plugins");
			output.println("====================");
			fixComXnifeObjectQueryPlanParser(aWorkspace);
			fixComProfidataXentisJavamis(aWorkspace);

			output.println("");
			output.println("Import products/features/projects");
			output.println("=================================");
			importProjectsOfProduct(aWorkspace, "/URRExtensions/PDE-Targets & Launcher", "products/xc.one.server.product", XCImportConfiguration.getInstance(), URRImportConfiguration.getInstance());
			importProjectsOfProduct(aWorkspace, "/URRExtensions/PDE-Targets & Launcher", "products/xc.one.client.product", XCImportConfiguration.getInstance(), URRImportConfiguration.getInstance());
			importProjectsOfProduct(aWorkspace, "/xentis/xc_one/main/_com.profidata.xc.one.server.build.product", "xc.one.server.product", XCImportConfiguration.getInstance(), URRImportConfiguration.getInstance());
			importProjectsOfProduct(aWorkspace, "/xentis/xc_one/main/_com.profidata.xc.one.client.build.product", "xc.one.client.product", XCImportConfiguration.getInstance(), URRImportConfiguration.getInstance());

			output.println("");
			output.println("Import missing features/projects");
			output.println("================================");

			output.println("");
			output.println("Add additional package dependencies for Eclipse IDE");
			output.println("===================================================");
			addAdditionalPackageDependencies(aWorkspace);

			output.println("");
			output.println("Add additional project source dependencies for Eclipse IDE");
			output.println("==========================================================");
			addAdditionalProjectDependencies(aWorkspace);

			output.println("");
			output.println("Ignore 'target' folder for every project in Eclipse IDE");
			output.println("=======================================================");
			IgnoreProjectFolder.run("target", output, error);
		}
		finally {
			if (aAutoBuildWasEnabled) {
				enableAutoBuild(aWorkspace);
			}
		}

		try {
			aWorkspace.getRoot().refreshLocal(IResource.DEPTH_INFINITE, null);
		}
		catch (CoreException theCause) {
			error.print("Could not refresh workspace: " + theCause.getMessage());
		}
	}

	private void addAdditionalPackageDependencies(IWorkspace theWorkspace) {
		PackageDependencyConfiguration.additionalBundlePackageDependencies.keySet().stream()
				.forEach(theProjectName -> {
					output.println("Verify additional dependencies in MANIFEST.MF of project '" + theProjectName + "'");
					ProjectWrapper.of(theWorkspace, theProjectName)
							.asJavaProject()
							.addPackageDependenciesToPluginManifest(() -> PackageDependencyConfiguration.additionalBundlePackageDependencies.get(theProjectName))
							.refresh();
				});

		PackageDependencyConfiguration.additionalTestFragmentPackageDependencies.keySet().stream()
				.forEach(theProjectName -> {
					output.println("Verify additional dependencies in MANIFEST.MF of test fragment '" + theProjectName + "'");
					ProjectWrapper.of(theWorkspace, theProjectName)
							.asJavaProject()
							.addPackageDependenciesToPluginManifest(() -> PackageDependencyConfiguration.additionalTestFragmentPackageDependencies.get(theProjectName))
							.refresh();
				});
	}

	private void addAdditionalProjectDependencies(IWorkspace theWorkspace) {
		ProjectDependencyConfiguration.additionalProjectDependencies.keySet().stream()
				.forEach(theProjectName -> {
					output.println("Verify additional project source dependencies of project '" + theProjectName + "'");
					ProjectWrapper.of(theWorkspace, theProjectName)
							.asJavaProject()
							.addProjectDependenciesToProject(() -> ProjectDependencyConfiguration.additionalProjectDependencies.get(theProjectName))
							.refresh();
				});
	}

	private void convertProjectFromGradleToPlugin(IWorkspace theWorkspace, String theProjectName) {
		ProjectWrapper aProjectWrapper = ProjectWrapper.of(theWorkspace, theProjectName);

		if (aProjectWrapper.isExisting() && !aProjectWrapper.isOpen()) {
			aProjectWrapper.open();
		}

		aProjectWrapper.asJavaProject();
		if (aProjectWrapper.isOpen() && aProjectWrapper.hasNature(ProjectConstants.GRADLE_NATURE_ID)) {
			output.println("Remove Gradle nature from project: " + theProjectName);
			aProjectWrapper
					.removeNature(ProjectConstants.GRADLE_NATURE_ID)
					.removeClasspathEntry(new Path(ProjectConstants.GRADLE_CLASSPATH_ID))
					.refresh();
		}

		if (aProjectWrapper.isOpen() && !aProjectWrapper.hasNature(ProjectConstants.PLUGIN_NATURE_ID)) {
			output.println("Add Plugin nature to project: " + theProjectName);
			aProjectWrapper
					.addNature(ProjectConstants.PLUGIN_NATURE_ID)
					.addClasspathEntry(theProject -> JavaCore.newContainerEntry(new Path(ProjectConstants.PLUGIN_CLASSPATH_ID)))
					.createPluginManifest("JavaSE-1.8", () -> Collections.emptySet())
					.refresh();
		}

		if (aProjectWrapper.isOpen() && !aProjectWrapper.hasClasspathEntry(new Path(ProjectConstants.PLUGIN_CLASSPATH_ID))) {
			aProjectWrapper
					.addClasspathEntry(theProject -> JavaCore.newContainerEntry(new Path(ProjectConstants.PLUGIN_CLASSPATH_ID)))
					.refresh();
		}

		if (aProjectWrapper.hasError()) {
			error.println("Exchange Gradle with Plugin nature for project: " + theProjectName + "' failed:\n-> " + aProjectWrapper.getErrorMessage());
		}
	}

	private void fixComXnifeObjectQueryPlanParser(IWorkspace theWorkspace) {
		ProjectWrapper aProjectWrapper = ProjectWrapper.of(theWorkspace, "com.xnife.objectquery.plan.parser").asJavaProject();
		IPath aLibraryPath = aProjectWrapper.getProject().getLocation();

		aProjectWrapper.addClasspathEntry(theProject -> JavaCore.newLibraryEntry(aLibraryPath.append("antlr-runtime-3.2.jar"), null, null));
	}

	private void fixComProfidataXentisJavamis(IWorkspace theWorkspace) {
		ProjectWrapper aProjectWrapper = ProjectWrapper.of(theWorkspace, "com.profidata.xentis.javamis").asJavaProject();
		IPath aJavaContainerPath = new Path("org.eclipse.jdt.launching.JRE_CONTAINER/org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType/JavaSE-1.8");
		IClasspathEntry aJavaContainerClasspathEntry = aProjectWrapper.getClasspathEntry(aJavaContainerPath);

		if (aJavaContainerClasspathEntry != null && aJavaContainerClasspathEntry.getAccessRules().length == 0) {
			output.println("Set access rules in Java container definition of project '" + aProjectWrapper.getProject().getName() + "'");
			aProjectWrapper
					.removeClasspathEntry(aJavaContainerPath)
					.addClasspathEntry(theProject -> {
						return JavaCore.newContainerEntry(
								new Path("org.eclipse.jdt.launching.JRE_CONTAINER/org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType/JavaSE-1.8"),
								new IAccessRule[] {
										JavaCore.newAccessRule(new Path("com/sun/java/swing/plaf/windows/*"), IAccessRule.K_ACCESSIBLE),
										JavaCore.newAccessRule(new Path("sun/awt/shell/*"), IAccessRule.K_ACCESSIBLE) },
								null,
								false);
					});
		}

		IPath aProvidedLibraryPath = aProjectWrapper.getProject().getLocation().append("provided");

		aProjectWrapper.addClasspathEntry(theProject -> JavaCore.newLibraryEntry(aProvidedLibraryPath.append("atdl4j.jar"), null, null));
		aProjectWrapper.addClasspathEntry(theProject -> JavaCore.newLibraryEntry(aProvidedLibraryPath.append("com.profidata.xentis.env.server.jar"), null, null, null, null, true));
		aProjectWrapper.addClasspathEntry(theProject -> JavaCore.newLibraryEntry(aProvidedLibraryPath.append("com.profidata.xentis.jni.jar"), null, null, null, null, true));
		aProjectWrapper.addClasspathEntry(theProject -> JavaCore.newLibraryEntry(aProvidedLibraryPath.append("com.profidata.xentis.ratex.jar"), null, null, null, null, true));
		aProjectWrapper.addClasspathEntry(theProject -> JavaCore.newLibraryEntry(aProvidedLibraryPath.append("com.profidata.xentis.sn.jar"), null, null, null, null, true));
		aProjectWrapper.addClasspathEntry(theProject -> JavaCore.newLibraryEntry(aProvidedLibraryPath.append("com.profidatagroup.util.keymigration.model.jar"), null, null));
		aProjectWrapper.addClasspathEntry(theProject -> JavaCore.newLibraryEntry(aProvidedLibraryPath.append("foxtrot.jar"), null, null));
		aProjectWrapper.addClasspathEntry(theProject -> JavaCore.newLibraryEntry(aProvidedLibraryPath.append("hawtbuf.jar"), null, null, null, null, true));
		aProjectWrapper.addClasspathEntry(theProject -> JavaCore.newLibraryEntry(aProvidedLibraryPath.append("jaxrpc.jar"), null, null));
		aProjectWrapper.addClasspathEntry(theProject -> JavaCore.newLibraryEntry(aProvidedLibraryPath.append("org.jzy3d.jar"), null, null));
		aProjectWrapper.addClasspathEntry(theProject -> JavaCore.newLibraryEntry(aProvidedLibraryPath.append("ratex.jar"), null, null));

		aProjectWrapper
				.refresh();
		verifyFixFailed(aProjectWrapper);
	}

	private void verifyFixFailed(ProjectWrapper theProjectWrapper) {
		if (theProjectWrapper.hasError()) {
			error.println("Fix project: " + theProjectWrapper.getProject().getName() + "' failed:\n-> " + theProjectWrapper.getErrorMessage());
		}
	}

	@SuppressWarnings("restriction")
	private void importProjectsOfProduct(IWorkspace theWorkspace, String theProjectPath, String theProductFilePath, ImportConfiguration... theImportConfigurations) {
		ProjectWrapper aProjectWrapper = importProject(theWorkspace, theProjectPath);

		if (!aProjectWrapper.hasError()) {
			IPath aProductFilePath = new Path(theProductFilePath);
			IFile aProductFile = aProjectWrapper.getProject().getFile(aProductFilePath);

			if (aProductFile.exists()) {
				IProductModel aProductModel = new WorkspaceProductModel(aProductFile, false);
				IProduct aProduct = aProductModel.getProduct();

				try {
					aProductModel.load();

					for (IProductFeature aFeatureChild : aProduct.getFeatures()) {
						ImportConfiguration aImportConfiguration = findImportConfigurationFor(aFeatureChild.getId(), theImportConfigurations);

						if (aImportConfiguration != null) {
							ImportFeatureProject aImportFeatureProject = aImportConfiguration.getFeatureProject(aFeatureChild.getId());

							if (aImportFeatureProject != null) {
								String aFeatureProjectPath = aImportConfiguration.getRootProjectPath() + "/" + aImportFeatureProject.getPath() + "/" + aFeatureChild.getId();

								ProjectWrapper aFeatureProject = importProject(theWorkspace, aFeatureProjectPath);

								if (!aFeatureProject.hasError() && aImportFeatureProject.getContentPath() != null) {
									importProjectsOfFeature(aFeatureProject, aImportFeatureProject.getContentPath(), theImportConfigurations);
								}
							}
							else {
								error.println("No configuration found for feature '" + aFeatureChild.getId() + "'");
							}
						}

						else {
							error.println("No import configuration available for feature project '" + aFeatureChild.getId() + "' -> included from '" + aProduct.getName() + "'");
						}
					}
				}
				catch (CoreException theCause) {
					error.println("Loading product file '" + aProductFile + "' failed: " + theCause.getMessage());
				}
			}

			else {
				error.println("The product file does not exist: " + aProductFile);
			}
		}
	}

	private void importProjectsOfFeature(IWorkspace theWorkspace, String theProjectPath, ImportConfiguration... theImportConfigurations) {
		ProjectWrapper aProjectWrapper = importProject(theWorkspace, theProjectPath);

		if (!aProjectWrapper.hasError()) {
			ImportConfiguration aImportConfiguration = findImportConfigurationFor(aProjectWrapper.getProject().getName(), theImportConfigurations);

			if (aImportConfiguration != null) {
				ImportFeatureProject aImportFeatureProject = aImportConfiguration.getFeatureProject(aProjectWrapper.getProject().getName());

				if (aImportFeatureProject != null) {
					importProjectsOfFeature(aProjectWrapper, aImportFeatureProject.getContentPath(), theImportConfigurations);
				}
				else {
					error.println("No configuration found for feature '" + aProjectWrapper.getProject().getName() + "'");
				}
			}

			else {
				error.println("No import configuration available for feature project '" + aProjectWrapper.getProject().getName() + "'");
			}
		}
	}

	@SuppressWarnings("restriction")
	private void importProjectsOfFeature(ProjectWrapper theFeatureProject, String theContentPath, ImportConfiguration... theImportConfigurations) {
		if (theFeatureProject.hasNature(ProjectConstants.FEATURE_NATURE_ID)) {
			try {
				IFeatureModel aFeatureModel = new WorkspaceFeatureModel(PDEProject.getFeatureXml(theFeatureProject.getProject()));
				IFeature aFeature = aFeatureModel.getFeature();

				aFeatureModel.load();

				for (IFeatureChild aFeatureChild : aFeature.getIncludedFeatures()) {
					ImportConfiguration aImportConfiguration = findImportConfigurationFor(aFeatureChild.getId(), theImportConfigurations);

					if (aImportConfiguration != null) {
						ImportFeatureProject aIncludedFeatureProject = aImportConfiguration.getFeatureProject(aFeatureChild.getId());
						String aFeatureProjectPath = "";

						if (aIncludedFeatureProject == null) {
							error.println("Feature project '" + aFeatureChild.getId() + "' not available -> included from '" + theFeatureProject.getProject().getName() + "'");
							continue;
						}

						if (!aImportConfiguration.getRootProjectPath().isEmpty()) {
							aFeatureProjectPath += aImportConfiguration.getRootProjectPath() + "/";
						}
						if (!aIncludedFeatureProject.getPath().isEmpty()) {
							aFeatureProjectPath += aIncludedFeatureProject.getPath() + "/";
						}
						aFeatureProjectPath += aFeatureChild.getId();

						ProjectWrapper aChildFeatureProject = importProject(theFeatureProject.getProject().getWorkspace(), aFeatureProjectPath);
						if (!aChildFeatureProject.hasError() && aIncludedFeatureProject.getContentPath() != null) {
							importProjectsOfFeature(aChildFeatureProject, aIncludedFeatureProject.getContentPath(), theImportConfigurations);
						}

						for (IFeaturePlugin aPlugin : aFeature.getPlugins()) {
							String aPluginProjectPath = aImportConfiguration.getRootProjectPath() + "/" + theContentPath + "/" + aPlugin.getId();

							importProject(theFeatureProject.getProject().getWorkspace(), aPluginProjectPath);
						}
					}

					else {
						error.println("No import configuration available for feature project '" + aFeatureChild.getId() + "' -> included from '" + theFeatureProject.getProject().getName() + "'");
					}
				}
			}
			catch (CoreException theCause) {
				error.println("Loading feature for project '" + theFeatureProject.getProject().getName() + "' failed: " + theCause.getMessage());
			}
		}
	}

	private ImportConfiguration findImportConfigurationFor(String theFeatureId, ImportConfiguration... theImportConfigurations) {
		return Arrays.stream(theImportConfigurations)
				.filter(theImportConfiguration -> theImportConfiguration.getFeatureProject(theFeatureId) != null)
				.findFirst().orElse(null);
	}

	private ProjectWrapper importProject(IWorkspace theWorkspace, String theProjectPath) {
		IPath aProjectLocation = theWorkspace.getRoot().getLocation().append(theProjectPath);
		String aProjectName = aProjectLocation.lastSegment();

		ProjectWrapper aProjectWrapper = ProjectWrapper.of(theWorkspace, aProjectName);

		if (!aProjectWrapper.isExisting()) {
			output.println("Import project '" + aProjectName + "' from '" + aProjectLocation + "'");
			aProjectWrapper.importProject(theWorkspace, aProjectLocation)
					.open();
		}
		if (aProjectWrapper.hasError()) {
			error.println("Importing of project from path '" + theProjectPath + "' failed: :\n-> " + aProjectWrapper.getErrorMessage());
		}
		return aProjectWrapper;
	}

	private boolean disableAutoBuild(IWorkspace theWorkspace) {
		IWorkspaceDescription aWorkspaceDescription = theWorkspace.getDescription();

		if (aWorkspaceDescription.isAutoBuilding()) {
			try {
				output.println("Disable autobuild in workspace\n");
				aWorkspaceDescription.setAutoBuilding(false);
				theWorkspace.setDescription(aWorkspaceDescription);
				return true;
			}
			catch (CoreException theCause) {
				error.println("Disable autobuild failed");
			}
		}
		return false;
	}

	private void enableAutoBuild(IWorkspace theWorkspace) {
		IWorkspaceDescription aWorkspaceDescription = theWorkspace.getDescription();

		if (!aWorkspaceDescription.isAutoBuilding()) {
			try {
				output.println("\nRe-enable autobuild in workspace");
				aWorkspaceDescription.setAutoBuilding(true);
				theWorkspace.setDescription(aWorkspaceDescription);
			}
			catch (CoreException theCause) {
				error.println("Enable autobuild failed");
			}
		}
	}
}
