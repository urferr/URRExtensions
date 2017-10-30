package org.plugination.featureworkingsets;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.internal.ui.packageview.PackageExplorerPart;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.pde.internal.core.PDECore;
import org.eclipse.pde.internal.core.feature.WorkspaceFeatureModel;
import org.eclipse.pde.internal.core.ifeature.IFeatureModel;
import org.eclipse.pde.internal.core.ifeature.IFeaturePlugin;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.progress.UIJob;

public class SyncFeaturesWithWorkingSetsHandler
  extends AbstractHandler
{
  public Object execute(ExecutionEvent event)
    throws ExecutionException
  {
    if (MessageDialog.openConfirm(HandlerUtil.getActiveShell(event), "Warning", "This will remove all working sets not ending with a dot (.) and then read all features from the open projects and your targetplatform, and then add a feature Working Sets if a plugin in that feature is in your open projects"))
    {
      UIJob job = new UIJob("Generating working sets")
      {
        public IStatus runInUIThread(IProgressMonitor monitor)
        {
          IFeatureModel[] featureModels = PDECore.getDefault().getFeatureModelManager().getModels();
          monitor.beginTask("Generating working sets", featureModels.length);
          try
          {
            IWorkingSetManager workingSetManager = PlatformUI.getWorkbench().getWorkingSetManager();
            IWorkingSet[] allWorkingSets = workingSetManager.getAllWorkingSets();
            IWorkingSet[] arrayOfIWorkingSet1;
            int j = (arrayOfIWorkingSet1 = allWorkingSets).length;
            for (int i = 0; i < j; i++)
            {
              IWorkingSet workingSet = arrayOfIWorkingSet1[i];
              if ((!workingSet.getName().endsWith(".")) && (!workingSet.getName().equals("Other Projects"))) {
                workingSetManager.removeWorkingSet(workingSet);
              }
            }
            List<IProject> pluginProjects = IDEUtils.getPluginProjects();
            IFeatureModel[] arrayOfIFeatureModel1;
            int k = (arrayOfIFeatureModel1 = featureModels).length;
            for (j = 0; j < k; j++)
            {
              IFeatureModel featureModel = arrayOfIFeatureModel1[j];
              if (monitor.isCanceled()) {
                return Status.CANCEL_STATUS;
              }
              String id = featureModel.getFeature().getTranslatableLabel() + " (" + featureModel.getFeature().getId() + ")";
              monitor.subTask("Processing: " + id);
              IFeaturePlugin[] featurePlugins = featureModel.getFeature().getPlugins();
              List<IProject> plugins = IDEUtils.toPluginList(pluginProjects, featurePlugins);
              if (!plugins.isEmpty())
              {
                if ((featureModel instanceof WorkspaceFeatureModel)) {
                  plugins.add(featureModel.getUnderlyingResource().getProject());
                }
                IWorkingSet workingSet = workingSetManager.createWorkingSet(id, (IAdaptable[])plugins.toArray(new IAdaptable[plugins.size()]));
                workingSet.setId("org.eclipse.jdt.ui.JavaWorkingSetPage");
                workingSetManager.addWorkingSet(workingSet);
              }
              monitor.worked(1);
            }
            PackageExplorerPart explorer = SyncFeaturesWithWorkingSetsHandler.this.getActivePackageExplorer();
            if (explorer != null)
            {
              explorer.rootModeChanged(2);
              IWorkingSet[] sortedWorkingSets = workingSetManager.getAllWorkingSets();
              explorer.getWorkingSetModel().addWorkingSets(sortedWorkingSets);
              explorer.getWorkingSetModel().configured();
            }
          }
          finally
          {
            monitor.done();
          }
          monitor.done();
          
          return Status.OK_STATUS;
        }
      };
      job.setUser(true);
      job.schedule();
    }
    return null;
  }
  
  private PackageExplorerPart getActivePackageExplorer()
  {
    final Object[] findView = new Object[1];
    Display.getDefault().syncExec(new Runnable()
    {
      public void run()
      {
        findView[0] = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView("org.eclipse.jdt.ui.PackageExplorer");
        if (findView[0] == null) {
          try
          {
            findView[0] = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView("org.eclipse.jdt.ui.PackageExplorer");
          }
          catch (PartInitException e)
          {
            e.printStackTrace();
          }
        }
      }
    });
    return (PackageExplorerPart)findView[0];
  }
}
