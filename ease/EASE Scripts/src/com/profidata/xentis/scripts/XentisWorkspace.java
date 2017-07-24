package com.profidata.xentis.scripts;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Collections;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
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

import com.profidata.xentis.config.ImportConfiguration;
import com.profidata.xentis.config.ImportConfiguration.ImportFeatureProject;
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
		output.println("Import products/features/projects");
		output.println("===========================");
		importProjectsOfProduct(aWorkspace, "/URRExtensions/PDE-Targets & Launcher", "products/xc.one.server.product");
		importProjectsOfProduct(aWorkspace, "/URRExtensions/PDE-Targets & Launcher", "products/xc.one.client.product");

		output.println("");
		output.println("Import missing features/projects");
		output.println("================================");
		importProjectsOfFeature(aWorkspace, "xentis/xc_bld/_com.profidata.xc.one.all.build.feature");
		importProjectsOfFeature(aWorkspace, "xentis/JavAMIS/_com.profidata.xc.one.client.backoffice.feature");

		try {
			aWorkspace.getRoot().refreshLocal(IResource.DEPTH_INFINITE, null);
		}
		catch (CoreException theCause) {
			error.print("Could not refresh workspace: " + theCause.getMessage());
		}
	}

	private void convertProjectFromGradleToPlugin(IWorkspace theWorkspace, String theProjectName) {
		ProjectWrapper aProjectWrapper = ProjectWrapper.of(theWorkspace, theProjectName);

		if (aProjectWrapper.isOpen() && aProjectWrapper.hasNature(ProjectConstants.GRADLE_NATURE_ID)) {
			output.println("Exchange Gradle with Plugin nature for project: " + theProjectName);
			aProjectWrapper
					.asJavaProject()
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

	@SuppressWarnings("restriction")
	private void importProjectsOfProduct(IWorkspace theWorkspace, String theProjectPath, String theProductFilePath) {
		ProjectWrapper aProjectWrapper = importProject(theWorkspace, theProjectPath);

		if (!aProjectWrapper.hasError()) {
			IPath aProductFilePath = new Path(theProductFilePath);
			IFile aProductFile = aProjectWrapper.getProject().getFile(aProductFilePath);

			if (aProductFile.exists()) {
				IProductModel aProductModel = new WorkspaceProductModel(aProductFile, false);
				IProduct aProduct = aProductModel.getProduct();
				ImportConfiguration aImportConfiguration = ImportConfiguration.getInstance();

				try {
					aProductModel.load();

					for (IProductFeature aFeatureChild : aProduct.getFeatures()) {
						ImportFeatureProject aImportFeatureProject = aImportConfiguration.getFeatureProject(aFeatureChild.getId());

						if (aImportFeatureProject != null) {
							String aFeatureProjectPath = aImportConfiguration.getXentisRootProjectPath() + "/" + aImportFeatureProject.getPath() + "/" + aFeatureChild.getId();

							ProjectWrapper aFeatureProject = importProject(theWorkspace, aFeatureProjectPath);

							if (!aFeatureProject.hasError() && aImportFeatureProject.getContentPath() != null) {
								importProjectsOfFeature(aFeatureProject, aImportFeatureProject.getContentPath());
							}
						}
						else {
							error.println("No configuration found for feature '" + aFeatureChild.getId() + "'");
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

	private void importProjectsOfFeature(IWorkspace theWorkspace, String theProjectPath) {
		ProjectWrapper aProjectWrapper = importProject(theWorkspace, theProjectPath);

		if (!aProjectWrapper.hasError()) {
			ImportConfiguration aImportConfiguration = ImportConfiguration.getInstance();
			ImportFeatureProject aImportFeatureProject = aImportConfiguration.getFeatureProject(aProjectWrapper.getProject().getName());

			if (aImportFeatureProject != null) {
				importProjectsOfFeature(aProjectWrapper, aImportFeatureProject.getContentPath());
			}
			else {
				error.println("No configuration found for feature '" + aProjectWrapper.getProject().getName() + "'");
			}
		}
	}

	@SuppressWarnings("restriction")
	private void importProjectsOfFeature(ProjectWrapper theFeatureProject, String theContentPath) {
		ImportConfiguration aImportConfiguration = ImportConfiguration.getInstance();

		if (theFeatureProject.hasNature(ProjectConstants.FEATURE_NATURE_ID)) {
			try {
				IFeatureModel aFeatureModel = new WorkspaceFeatureModel(PDEProject.getFeatureXml(theFeatureProject.getProject()));
				IFeature aFeature = aFeatureModel.getFeature();

				aFeatureModel.load();

				for (IFeatureChild aFeatureChild : aFeature.getIncludedFeatures()) {
					ImportFeatureProject aImportFeatureProject = aImportConfiguration.getFeatureProject(aFeatureChild.getId());
					String aFeatureProjectPath = aImportConfiguration.getXentisRootProjectPath() + "/" + aImportFeatureProject.getPath() + "/" + aFeatureChild.getId();

					ProjectWrapper aChildFeatureProject = importProject(theFeatureProject.getProject().getWorkspace(), aFeatureProjectPath);
					if (!aChildFeatureProject.hasError() && aImportFeatureProject.getContentPath() != null) {
						importProjectsOfFeature(aChildFeatureProject, aImportFeatureProject.getContentPath());
					}
				}

				for (IFeaturePlugin aPlugin : aFeature.getPlugins()) {
					String aPluginProjectPath = aImportConfiguration.getXentisRootProjectPath() + "/" + theContentPath + "/" + aPlugin.getId();

					importProject(theFeatureProject.getProject().getWorkspace(), aPluginProjectPath);
				}
			}
			catch (CoreException theCause) {
				error.println("Loading feature for project '" + theFeatureProject.getProject().getName() + "' failed: " + theCause.getMessage());
			}
		}
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

}
