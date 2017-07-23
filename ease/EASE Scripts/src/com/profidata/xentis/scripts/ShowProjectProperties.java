package com.profidata.xentis.scripts;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
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
import org.eclipse.pde.internal.core.feature.WorkspaceFeatureModel;
import org.eclipse.pde.internal.core.ifeature.IFeature;
import org.eclipse.pde.internal.core.ifeature.IFeatureChild;
import org.eclipse.pde.internal.core.ifeature.IFeatureModel;
import org.eclipse.pde.internal.core.ifeature.IFeaturePlugin;
import org.eclipse.pde.internal.core.iproduct.IProduct;
import org.eclipse.pde.internal.core.iproduct.IProductFeature;
import org.eclipse.pde.internal.core.iproduct.IProductModel;
import org.eclipse.pde.internal.core.iproduct.IProductPlugin;
import org.eclipse.pde.internal.core.product.WorkspaceProductModel;
import org.eclipse.pde.internal.core.project.PDEProject;

import com.profidata.xentis.util.ProjectConstants;

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
					if (theProject.isOpen()) {
						listNature(theProject);
						listClasspath(theProject);
						listPackages(theProject);
						listFeature(theProject);
						listProduct(theProject);
					}
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

		try {
			if (theProject.hasNature(JavaCore.NATURE_ID)) {
				IJavaProject aJavaProject = JavaCore.create(theProject);

				out.println(INDENT + "Classpath entries:");
				Arrays.stream(aJavaProject.getRawClasspath())
						.forEach(
								theClasspathEntry -> out.println(
										INDENT + "- " + theClasspathEntry.getPath() + " - "
												+ "Entry: " + toEntryKind(theClasspathEntry.getEntryKind()) + " - " + "Content: "
												+ toContentKind(theClasspathEntry.getContentKind())));
				out.println(INDENT + "- " + aJavaProject.getOutputLocation() + " - " + "output");
			}
		}
		catch (CoreException theCause) {
			err.println("Could not access class path of project '" + theProject.getName() + "': " + theCause.getMessage());
		}

	}

	private void listFeature(IProject theProject) {
		try {
			if (theProject.hasNature(ProjectConstants.FEATURE_NATURE_ID)) {
				IFeatureModel aFeatureModel = new WorkspaceFeatureModel(PDEProject.getFeatureXml(theProject));
				IFeature aFeature = aFeatureModel.getFeature();

				aFeatureModel.load();

				out.println(INDENT + "Referenced plugins:");
				for (IFeaturePlugin aPlugin : aFeature.getPlugins()) {
					out.println(INDENT + "- " + aPlugin.getId());
				}

				out.println(INDENT + "Included features:");
				for (IFeatureChild aFeatureChild : aFeature.getIncludedFeatures()) {
					out.println(INDENT + "- " + aFeatureChild.getId());
				}
			}
		}
		catch (CoreException theCause) {
			err.println("Could not access feature descriptor of project '" + theProject.getName() + "': " + theCause.getMessage());
		}
	}

	private void listProduct(IProject theProject) {
		try {
			if (theProject.hasNature(ProjectConstants.PLUGIN_NATURE_ID)) {
				out.println(INDENT + "Included products:");
				for (IResource aProjectMember : theProject.members()) {
					if (aProjectMember instanceof IFile && Optional.ofNullable(aProjectMember.getFileExtension()).map(theExtension -> theExtension.equals("product")).orElse(false)) {
						IFile aProjectProductFile = (IFile) aProjectMember;

						out.println(INDENT + "- " + aProjectProductFile.getName());
						IProductModel aProductModel = new WorkspaceProductModel(aProjectProductFile, false);
						IProduct aProduct = aProductModel.getProduct();

						aProductModel.load();

						out.println(INDENT + INDENT + "Referenced plugins:");
						for (IProductPlugin aPlugin : aProduct.getPlugins()) {
							out.println(INDENT + INDENT + "- " + aProduct.getId());
						}

						out.println(INDENT + INDENT + "Included features:");
						for (IProductFeature aFeatureChild : aProduct.getFeatures()) {
							out.println(INDENT + INDENT + "- " + aFeatureChild.getId());
						}
					}
				}
			}
		}
		catch (CoreException theCause) {
			err.println("Could not access feature descriptor of project '" + theProject.getName() + "': " + theCause.getMessage());
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

	public void listPackages(IProject theProject) {
		try {
			if (theProject.hasNature(JavaCore.NATURE_ID)) {
				IJavaProject aJavaProject = JavaCore.create(theProject);
				IPackageFragmentRoot[] allPackageFragmentRoots;

				allPackageFragmentRoots = aJavaProject.getPackageFragmentRoots();

				out.println(INDENT + "Package fragment roots:");
				for (IPackageFragmentRoot aPackageFragmentRoot : allPackageFragmentRoots) {
					if (aPackageFragmentRoot.getKind() == IPackageFragmentRoot.K_SOURCE) {
						listPackages(aPackageFragmentRoot);
					}
				}
			}
		}
		catch (CoreException theCause) {
			err.println("Could not access package fragment roots of project '" + theProject.getName() + "': " + theCause.getMessage());
		}
	}

	private void listPackages(IJavaElement theJavaElement) throws JavaModelException {
		if (theJavaElement instanceof IPackageFragmentRoot) {
			IPackageFragmentRoot aPackageFragmentRoot = (IPackageFragmentRoot) theJavaElement;
			out.println(INDENT + "Package fragment root: " + theJavaElement.getElementName());

		}
		else if (theJavaElement instanceof IPackageFragment) {
			IPackageFragment aPackageFragment = (IPackageFragment) theJavaElement;
			out.println(INDENT + "Package fragment: " + theJavaElement.getElementName());
		}
		else if (theJavaElement instanceof ICompilationUnit) {
			ICompilationUnit aCompilationUnit = (ICompilationUnit) theJavaElement;
			out.println(INDENT + INDENT + "Compilation unit: " + theJavaElement.getElementName());

			for (IImportDeclaration aImport : aCompilationUnit.getImports()) {
				if (Flags.isStatic(aImport.getFlags())) {
					out.println(INDENT + INDENT + INDENT + "Static import: " + aImport.getElementName() + " -> " + extractPackage(aImport));

				}
				else {
					out.println(INDENT + INDENT + INDENT + "Import: " + aImport.getElementName() + " -> " + extractPackage(aImport));
				}
			}

		}
		else {
			return;
		}

		if (theJavaElement instanceof IParent) {
			IParent aContainerElement = (IParent) theJavaElement;

			for (IJavaElement aJavaElement : aContainerElement.getChildren()) {
				listPackages(aJavaElement);
			}
		}
	}

	private String extractPackage(IImportDeclaration theImportDeclaration) throws JavaModelException {
		final int skipLastElements = Flags.isStatic(theImportDeclaration.getFlags()) ? 2 : 1;

		List<String> allImportTokens = new ArrayList<>(
				Arrays.asList(theImportDeclaration.getElementName().split("\\.")));
		Collections.reverse(allImportTokens);

		List<String> aImportPackageTokens = allImportTokens.stream().skip(skipLastElements)
				.filter(thePackageToken -> Character.isLowerCase(thePackageToken.codePointAt(0)))
				.collect(Collectors.toList());
		Collections.reverse(aImportPackageTokens);

		return aImportPackageTokens.stream().collect(Collectors.joining("."));
	}
}
