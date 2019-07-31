package com.tle.jpfclasspath.properties;

import com.tle.jpfclasspath.JPFClasspathLog;
import com.tle.jpfclasspath.JPFClasspathPlugin;
import com.tle.jpfclasspath.model.JPFProject;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.osgi.service.prefs.BackingStoreException;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 *
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class JPFAssignHandler extends AbstractHandler {
  /** The constructor. */
  public JPFAssignHandler() {}

  /**
   * the command has been executed, so extract extract the needed information from the application
   * context.
   */
  @SuppressWarnings("nls")
  @Override
  public Object execute(ExecutionEvent event) throws ExecutionException {
    ISelection sel = HandlerUtil.getCurrentSelectionChecked(event);
    if (sel instanceof IStructuredSelection) {
      final Object[] selections = ((IStructuredSelection) sel).toArray();
      IProject defaultSelection = null;
      IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
      for (Object selObj : selections) {
        IJavaProject project = (IJavaProject) selObj;
        String registry =
            JPFProject.getPreferences(project.getProject())
                .get(JPFClasspathPlugin.PREF_REGISTRY_NAME, "");
        if (registry.length() > 0) {
          IProject selected = root.getProject(registry);
          if (selected.isAccessible()) {
            defaultSelection = selected;
            break;
          }
        }
      }

      IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
      JPFProjectSelectionDialog dialog =
          new JPFProjectSelectionDialog(window.getShell(), defaultSelection);
      dialog.setTitle("Assign JPF registry");
      dialog.setMessage("Please select a JPF registry project");
      dialog.open();
      final Object[] result = dialog.getResult();
      if (result == null) {
        return null;
      }
      new Job("Assign Registries") {
        @Override
        protected IStatus run(IProgressMonitor monitor) {
          String projectName = ((IProject) result[0]).getName();
          for (Object selection : selections) {
            IJavaProject project = (IJavaProject) selection;
            IEclipsePreferences prefs =
                new ProjectScope(project.getProject()).getNode(JPFClasspathPlugin.PLUGIN_ID);
            prefs.put(JPFClasspathPlugin.PREF_REGISTRY_NAME, projectName);
            try {
              prefs.flush();
            } catch (BackingStoreException e) {
              JPFClasspathLog.logError(e);
            }
          }
          return Status.OK_STATUS;
        }
      }.schedule();
    }
    return null;
  }
}
