package com.tle.jpfclasspath.model;

import com.tle.jpfclasspath.JPFClasspathLog;
import com.tle.jpfclasspath.JPFJarNature;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.URIUtil;
import org.eclipse.jdt.core.JavaCore;

@SuppressWarnings("nls")
public class WorkspaceJarModelManager extends WorkspaceModelManager<RepoModel> {
  private Map<IFile, JarPluginModelImpl> jarModels = new HashMap<>();

  @Override
  protected boolean isInterestingProject(IProject project) {
    try {
      return project.isOpen() && project.hasNature(JPFJarNature.NATURE_ID);
    } catch (CoreException e) {
      JPFClasspathLog.logError(e);
      return false;
    }
  }

  @Override
  protected void createModel(IProject project, boolean notify) {
    RepoModel repoModel = new RepoModel(this, project);
    try {
      fModels.put(project, repoModel);
      if (notify) {
        addChange(repoModel, IModelProviderEvent.MODELS_ADDED);
      }
      IResource[] resources = project.members();
      for (IResource res : resources) {
        if (res.getType() == IResource.FILE) {
          IFile file = (IFile) res;
          if (isPluginJar(file)) {
            addJarModel(repoModel, file, notify);
          }
        }
      }
    } catch (CoreException e) {
      JPFClasspathLog.logError(e);
    }
  }

  private JarPluginModelImpl addJarModel(RepoModel repoModel, IFile file, boolean notify) {
    JarPluginModelImpl model = new JarPluginModelImpl(repoModel, file);
    repoModel.addJarModel(model);
    jarModels.put(file, model);
    if (notify) {
      addChange(model, IModelProviderEvent.MODELS_ADDED);
    }
    return model;
  }

  private boolean isPluginJar(IFile file) {
    if ("jar".equals(file.getFileExtension())) {
      URI jarURI = URIUtil.toJarURI(file.getLocationURI(), JPFProject.MANIFEST_PATH);
      try {
        JarEntry jarFile = ((JarURLConnection) jarURI.toURL().openConnection()).getJarEntry();
        return jarFile != null;
      } catch (IOException e) {
        // Bad zip
      }
    }
    return false;
  }

  @Override
  protected void removeModel(IProject project) {
    RepoModel repoModel = fModels.get(project);
    if (repoModel != null) {
      List<? extends IPluginModel> models = repoModel.getJarModels();
      for (IModel model : models) {
        jarModels.remove(model.getUnderlyingResource());
        addChange(model, IModelProviderEvent.MODELS_REMOVED);
      }
      addChange(repoModel, IModelProviderEvent.MODELS_REMOVED);
    }
  }

  private void removeJar(JarPluginModelImpl model) {
    RepoModel repoModel = model.getRepoModel();
    repoModel.removeJarModel(model);
    jarModels.remove(model.getUnderlyingResource());
    addChange(model, IModelProviderEvent.MODELS_REMOVED);
  }

  @Override
  protected void addListeners() {
    IWorkspace workspace = ResourcesPlugin.getWorkspace();
    workspace.addResourceChangeListener(this, IResourceChangeEvent.PRE_CLOSE);
    JavaCore.addPreProcessingResourceChangedListener(this, IResourceChangeEvent.POST_CHANGE);
  }

  @Override
  protected void handleFileDelta(IResourceDelta delta) {
    IFile file = (IFile) delta.getResource();
    JarPluginModelImpl existing = jarModels.get(file);
    if (delta.getKind() == IResourceDelta.REMOVED && existing != null) {
      removeJar(existing);
    } else if (delta.getKind() == IResourceDelta.ADDED) {
      if (isPluginJar(file)) {
        addJarModel(fModels.get(file.getProject()), file, true);
      }
    } else if (delta.getKind() == IResourceDelta.CHANGED && existing != null) {
      existing.reload();
      addChange(existing, IModelProviderEvent.MODELS_CHANGED);
    }
  }

  @Override
  protected void removeListeners() {
    ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
    JavaCore.removePreProcessingResourceChangedListener(this);
    super.removeListeners();
  }

  public IRepoModel[] getRepoModels() {
    initialize();
    return fModels.values().toArray(new IRepoModel[fModels.size()]);
  }

  public IPluginModel getPluginModel(IFile jarFile) {
    initialize();
    return jarModels.get(jarFile);
  }
}
