package org.plugination.featureworkingsets;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.pde.core.plugin.IPluginModelBase;
import org.eclipse.pde.core.plugin.PluginRegistry;
import org.eclipse.pde.internal.core.ifeature.IFeaturePlugin;

public class IDEUtils
{
  public static List<IProject> toPluginList(IFeaturePlugin[] featurePlugins)
  {
    return toPluginList(getPluginProjects(), featurePlugins);
  }
  
  public static List<IProject> toPluginList(List<IProject> pluginProjects, IFeaturePlugin[] featurePlugins)
  {
    List<IProject> result = new ArrayList();
    IFeaturePlugin[] arrayOfIFeaturePlugin;
    int j = (arrayOfIFeaturePlugin = featurePlugins).length;
    for (int i = 0; i < j; i++)
    {
      IFeaturePlugin featurePlugin = arrayOfIFeaturePlugin[i];
      if (featurePlugin.getId() != null) {
        for (IProject pluginProject : pluginProjects)
        {
          IPluginModelBase model = PluginRegistry.findModel(pluginProject);
          if ((model != null) && (model.getBundleDescription() != null) && (model.getBundleDescription().getSymbolicName() != null) && 
            (featurePlugin.getId().equals(model.getBundleDescription().getSymbolicName()))) {
            result.add(pluginProject);
          }
        }
      }
    }
    return result;
  }
  
  public static List<IProject> getPluginProjects()
  {
    IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
    IProject[] projects = root.getProjects();
    List<IProject> result = new ArrayList();
    IProject[] arrayOfIProject1;
    int j = (arrayOfIProject1 = projects).length;
    for (int i = 0; i < j; i++)
    {
      IProject project = arrayOfIProject1[i];
      try
      {
        if ((project.isOpen()) && (project.getDescription() != null) && (project.getDescription().hasNature("org.eclipse.pde.PluginNature"))) {
          result.add(project.getProject());
        }
      }
      catch (CoreException e)
      {
        e.printStackTrace();
      }
    }
    return result;
  }
}
