/**
 * ***************************************************************************** Copyright (c) 2006,
 * 2012 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html Contributors: IBM
 * Corporation - initial API and implementation
 * *****************************************************************************
 */
package com.tle.jpfclasspath.model;

import com.tle.jpfclasspath.JPFProjectNature;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.JavaCore;

public class WorkspacePluginModelManager extends WorkspaceModelManager<IPluginModel> {
  public static boolean isPluginProject(IProject project) {
    try {
      if (project.isOpen() && project.hasNature(JPFProjectNature.NATURE_ID))
        return JPFProject.getManifest(project).exists();
    } catch (CoreException e) {
      return false;
    }
    return false;
  }

  /** The workspace plug-in model manager is only interested in changes to plug-in projects. */
  @Override
  protected boolean isInterestingProject(IProject project) {
    return isPluginProject(project);
  }

  @Override
  protected void createModel(IProject project, boolean notify) {
    IPluginModel model = null;
    if (isPluginProject(project)) {
      model = new ProjectPluginModelImpl(this, project);
    }
    if (model != null) {
      fModels.put(project, model);
      if (notify) {
        addChange(model, IModelProviderEvent.MODELS_ADDED);
      }
    }
  }

  @Override
  protected void removeModel(IProject project) {
    IModel model = fModels != null ? fModels.remove(project) : null;
    if (model != null) {
      ((ProjectPluginModelImpl) model).stop();
    }
    addChange(model, IModelProviderEvent.MODELS_REMOVED);
  }

  /** Reacts to changes in files of interest to PDE */
  @Override
  protected void handleFileDelta(IResourceDelta delta) {
    IFile file = (IFile) delta.getResource();
    IProject project = file.getProject();
    if (file.equals(JPFProject.getManifest(project))) {
      handleBundleManifestDelta(file, delta);
    }
  }

  private void handleBundleManifestDelta(IFile file, IResourceDelta delta) {
    int kind = delta.getKind();
    IProject project = file.getProject();
    IPluginModel model = getPluginModel(project);
    if (kind == IResourceDelta.REMOVED && model != null) {
      removeModel(project);
      // switch to legacy plugin structure, if applicable
      createModel(project, true);
    } else if (kind == IResourceDelta.ADDED || model == null) {
      createModel(project, true);
    } else if (kind == IResourceDelta.CHANGED && (IResourceDelta.CONTENT & delta.getFlags()) != 0) {
      boolean wasFragment = model.isFragmentModel();
      model.reload();

      // Fragment-Host header was added or removed
      if (wasFragment != model.isFragmentModel()) {
        removeModel(project);
        createModel(project, true);
      } else {
        addChange(model, IModelProviderEvent.MODELS_CHANGED);
      }
    }
  }

  protected IPluginModel getPluginModel(IProject project) {
    initialize();
    return fModels.get(project);
  }

  @Override
  protected void addListeners() {
    IWorkspace workspace = ResourcesPlugin.getWorkspace();
    workspace.addResourceChangeListener(this, IResourceChangeEvent.PRE_CLOSE);
    JavaCore.addPreProcessingResourceChangedListener(this, IResourceChangeEvent.POST_CHANGE);
  }

  @Override
  protected void removeListeners() {
    ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
    JavaCore.removePreProcessingResourceChangedListener(this);
    super.removeListeners();
  }

  @Override
  protected boolean isInterestingFolder(IFolder folder) {
    return false;
  }

  public IPluginModel[] getPluginModels() {
    initialize();
    return fModels.values().toArray(new IPluginModel[fModels.size()]);
  }
}
