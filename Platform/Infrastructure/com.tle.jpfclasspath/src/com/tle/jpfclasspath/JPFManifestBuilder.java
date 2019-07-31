package com.tle.jpfclasspath;

import com.tle.jpfclasspath.model.IPluginModel;
import com.tle.jpfclasspath.model.IResolvedPlugin;
import com.tle.jpfclasspath.model.JPFPluginModelManager;
import com.tle.jpfclasspath.model.JPFProject;
import com.tle.jpfclasspath.model.ResolvedImport;
import com.tle.jpfclasspath.parser.ModelPluginManifest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;

public class JPFManifestBuilder extends IncrementalProjectBuilder {
  public static final String BUILDER_ID = JPFClasspathPlugin.PLUGIN_ID + ".manifestBuilder";

  class ManifestVisitor implements IResourceDeltaVisitor {
    boolean manifestChanged = false;

    public void reset() {
      manifestChanged = false;
    }

    @Override
    public boolean visit(IResourceDelta delta) throws CoreException {
      IResource resource = delta.getResource();
      if (resource.isDerived()) {
        return false;
      }
      if (resource.getType() == IResource.FILE) {
        IFile file = (IFile) resource;
        IProject project = resource.getProject();
        if (file.equals(JPFProject.getManifest(project))) {
          manifestChanged = true;
          return false;
        }
      }
      return true;
    }
  }

  private ManifestVisitor manifestVisitor = new ManifestVisitor();

  @Override
  protected IProject[] build(int kind, Map<String, String> args, IProgressMonitor monitor)
      throws CoreException {
    IProject project = getProject();
    IResourceDelta delta = getDelta(project);
    boolean doValidate = false;
    if (delta == null) {
      doValidate = true;
    } else {
      if (Boolean.TRUE.equals(project.getSessionProperty(JPFClasspathPlugin.TOUCH_PROJECT))) {
        project.setSessionProperty(JPFClasspathPlugin.TOUCH_PROJECT, null);
        doValidate = true;
      } else {
        manifestVisitor.reset();
        delta.accept(manifestVisitor);
        doValidate = manifestVisitor.manifestChanged;
      }
    }
    if (doValidate) {
      validateManifest(project);
    }
    return null;
  }

  private void validateManifest(IProject project) {
    IFile manifestFile = JPFProject.getManifest(project);
    try {
      cleanProblems(manifestFile, IResource.DEPTH_ZERO);
    } catch (CoreException e1) {
      // whatevs
    }
    IPluginModel model = JPFPluginModelManager.instance().findModel(project);
    if (model != null) {
      ModelPluginManifest parsedManifest = model.getParsedManifest();
      if (parsedManifest == null) {
        reportError(manifestFile, "Error parsing manifest");
        return;
      }

      IResolvedPlugin resolvedPlugin = JPFPluginModelManager.instance().getResolvedPlugin(model);
      if (resolvedPlugin == null) {
        reportError(manifestFile, "Manifest not registered");
        return;
      }
      List<ResolvedImport> imports = resolvedPlugin.getImports();
      for (ResolvedImport resolvedImport : imports) {
        IResolvedPlugin resolved = resolvedImport.getResolved();
        if (resolved.getPluginModel() == null && !resolvedImport.getPrerequisite().isOptional()) {
          String missingId = resolvedImport.getPrerequisite().getPluginId();
          reportError(
              manifestFile,
              "Plugin '"
                  + missingId
                  + "' cannot be found in registry '"
                  + resolvedPlugin.getRegistryName()
                  + "'");
        }
      }
    }
  }

  private void reportError(IResource resource, String message) {
    try {
      IMarker marker = resource.createMarker(JPFClasspathPlugin.MARKER_ID);
      marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
      marker.setAttribute(IMarker.MESSAGE, message);
      marker.setAttribute(IMarker.LINE_NUMBER, 1);
    } catch (CoreException e) {
      // nothing
    }
  }

  /*
   * (non-Javadoc)
   * @see
   * org.eclipse.core.resources.IncrementalProjectBuilder#clean(org.eclipse
   * .core.runtime.IProgressMonitor)
   */
  @Override
  protected void clean(IProgressMonitor monitor) throws CoreException {
    SubMonitor localmonitor = SubMonitor.convert(monitor, "JPF Manifest validation", 1);
    try {
      // clean problem markers on the project
      cleanProblems(getProject(), IResource.DEPTH_ZERO);
      // clean the manifest directory (since errors can be created on
      // manifest files with incorrect casing)
      IFile manifestFile = JPFProject.getManifest(getProject());
      cleanProblems(manifestFile.getParent(), IResource.DEPTH_ONE);
      localmonitor.worked(1);
    } finally {
      localmonitor.done();
    }
  }

  private void cleanProblems(IResource resource, int depth) throws CoreException {
    if (resource.exists()) {
      resource.deleteMarkers(JPFClasspathPlugin.MARKER_ID, true, depth);
    }
  }

  public static void addBuilderToProject(IProjectDescription description) {
    if (hasBuilder(description)) {
      return;
    }
    // Associate builder with project.
    ICommand[] cmds = description.getBuildSpec();
    ICommand newCmd = description.newCommand();
    newCmd.setBuilderName(BUILDER_ID);
    List<ICommand> newCmds = new ArrayList<ICommand>();
    newCmds.addAll(Arrays.asList(cmds));
    newCmds.add(newCmd);
    description.setBuildSpec(newCmds.toArray(new ICommand[newCmds.size()]));
  }

  public static boolean hasBuilder(IProjectDescription description) {
    // Look for builder.
    ICommand[] cmds = description.getBuildSpec();
    for (int j = 0; j < cmds.length; j++) {
      if (cmds[j].getBuilderName().equals(BUILDER_ID)) {
        return true;
      }
    }
    return false;
  }

  public static void removeBuilderFromProject(IProjectDescription description) {
    // Look for builder.
    int index = -1;
    ICommand[] cmds = description.getBuildSpec();
    for (int j = 0; j < cmds.length; j++) {
      if (cmds[j].getBuilderName().equals(BUILDER_ID)) {
        index = j;
        break;
      }
    }
    if (index == -1) return;

    // Remove builder from project.
    List<ICommand> newCmds = new ArrayList<ICommand>();
    newCmds.addAll(Arrays.asList(cmds));
    newCmds.remove(index);
    description.setBuildSpec(newCmds.toArray(new ICommand[newCmds.size()]));
  }

  public static void deleteMarkers(IProject project) {
    try {
      project.deleteMarkers(JPFClasspathPlugin.MARKER_ID, true, IResource.DEPTH_INFINITE);
    } catch (CoreException e) {
      // who cares
    }
  }
}
