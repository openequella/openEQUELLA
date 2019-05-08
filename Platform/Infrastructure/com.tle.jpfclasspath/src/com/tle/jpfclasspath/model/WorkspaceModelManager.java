package com.tle.jpfclasspath.model;

import com.tle.jpfclasspath.JPFClasspathLog;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

public abstract class WorkspaceModelManager<M extends IModel> extends AbstractModelManager
    implements IResourceChangeListener, IResourceDeltaVisitor {
  protected Map<IProject, M> fModels = null;
  private List<ModelChange> fChangedModels;

  static class ModelChange {
    IModel model;
    int type;

    public ModelChange(IModel model, int type) {
      this.model = model;
      this.type = type;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj instanceof ModelChange) {
        ModelChange change = (ModelChange) obj;
        IResource resource = change.model.getUnderlyingResource();
        int type = change.type;
        return model.getUnderlyingResource().equals(resource) && this.type == type;
      }
      return false;
    }

    @Override
    public int hashCode() {
      // for warning
      return super.hashCode();
    }
  }

  protected synchronized void initialize() {
    if (fModels != null) return;

    fModels = Collections.synchronizedMap(new HashMap<IProject, M>());
    IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
    for (int i = 0; i < projects.length; i++) {
      if (isInterestingProject(projects[i])) createModel(projects[i], false);
    }
    addListeners();
  }

  protected abstract boolean isInterestingProject(IProject project);

  protected abstract void createModel(IProject project, boolean notify);

  protected abstract void removeModel(IProject project);

  protected abstract void addListeners();

  @Override
  public void resourceChanged(IResourceChangeEvent event) {
    switch (event.getType()) {
      case IResourceChangeEvent.POST_CHANGE:
        handleResourceDelta(event.getDelta());
        processModelChanges();
        break;
      case IResourceChangeEvent.PRE_CLOSE:
        removeModel((IProject) event.getResource());
        processModelChanges();
        break;
    }
  }

  private void handleResourceDelta(IResourceDelta delta) {
    try {
      delta.accept(this);
    } catch (CoreException e) {
      JPFClasspathLog.logError(e);
    }
  }

  /*
   * (non-Javadoc)
   * @see
   * org.eclipse.core.resources.IResourceDeltaVisitor#visit(org.eclipse.core
   * .resources.IResourceDelta)
   */
  @Override
  public boolean visit(IResourceDelta delta) throws CoreException {
    if (delta != null) {

      final IResource resource = delta.getResource();
      if (!resource.isDerived()) {
        switch (resource.getType()) {
          case IResource.ROOT:
            return true;
          case IResource.PROJECT:
            {
              IProject project = (IProject) resource;
              int kind = delta.getKind();
              int flags = delta.getFlags();
              if (isInterestingProject(project)
                  && (kind == IResourceDelta.ADDED
                      || (flags & IResourceDelta.OPEN) != 0
                      || (kind == IResourceDelta.CHANGED
                          && (flags & IResourceDelta.DESCRIPTION) != 0
                          && !isProjectKnown(project)))) {
                createModel(project, true);
                return false;
              } else if (kind == IResourceDelta.REMOVED) {
                removeModel(project);
                return false;
              }
              return true;
            }
          case IResource.FOLDER:
            return isInterestingFolder((IFolder) resource);
          case IResource.FILE:
            // do not process
            if (isContentChange(delta)) {
              handleFileDelta(delta);
              return false;
            }
        }
      }
    }
    return false;
  }

  protected boolean isProjectKnown(IProject project) {
    return fModels.containsKey(project);
  }

  private boolean isContentChange(IResourceDelta delta) {
    int kind = delta.getKind();
    return (kind == IResourceDelta.ADDED
        || kind == IResourceDelta.REMOVED
        || (kind == IResourceDelta.CHANGED && (delta.getFlags() & IResourceDelta.CONTENT) != 0));
  }

  protected boolean isInterestingFolder(IFolder folder) {
    return false;
  }

  protected abstract void handleFileDelta(IResourceDelta delta);

  protected void addChange(Object model, int eventType) {
    if (model instanceof IModel) {
      if (fChangedModels == null) fChangedModels = new ArrayList<ModelChange>();
      ModelChange change = new ModelChange((IModel) model, eventType);
      if (!fChangedModels.contains(change)) fChangedModels.add(change);
    }
  }

  protected void processModelChanges() {
    processModelChanges("org.eclipse.pde.core.IModelProviderEvent", fChangedModels); // $NON-NLS-1$
    fChangedModels = null;
  }

  protected void processModelChanges(String changeId, List<ModelChange> changedModels) {
    if (changedModels == null) return;

    if (changedModels.size() == 0) {
      return;
    }

    ArrayList<IModel> added = new ArrayList<IModel>();
    ArrayList<IModel> removed = new ArrayList<IModel>();
    ArrayList<IModel> changed = new ArrayList<IModel>();
    for (ListIterator<ModelChange> li = changedModels.listIterator(); li.hasNext(); ) {
      ModelChange change = li.next();
      switch (change.type) {
        case IModelProviderEvent.MODELS_ADDED:
          added.add(change.model);
          break;
        case IModelProviderEvent.MODELS_REMOVED:
          removed.add(change.model);
          break;
        case IModelProviderEvent.MODELS_CHANGED:
          changed.add(change.model);
      }
    }

    int type = 0;
    if (added.size() > 0) type |= IModelProviderEvent.MODELS_ADDED;
    if (removed.size() > 0) type |= IModelProviderEvent.MODELS_REMOVED;
    if (changed.size() > 0) type |= IModelProviderEvent.MODELS_CHANGED;

    if (type != 0) {
      createAndFireEvent(changeId, type, added, removed, changed);
    }
  }

  protected void createAndFireEvent(
      String eventId,
      int type,
      Collection<IModel> added,
      Collection<IModel> removed,
      Collection<IModel> changed) {
    final ModelProviderEvent event =
        new ModelProviderEvent(
            this,
            type,
            added.toArray(new IModel[added.size()]),
            removed.toArray(new IModel[removed.size()]),
            changed.toArray(new IModel[changed.size()]));
    fireModelProviderEvent(event);
  }

  public void modelChanged(IModel model) {
    modelChangeJob.add(model);
    modelChangeJob.schedule();
  }

  class ModelsChangedJob extends Job {

    private List<IModel> models = new ArrayList<>();

    /** Constructs a new job. */
    public ModelsChangedJob() {
      super("Trigger Model Change");
      setRule(ResourcesPlugin.getWorkspace().getRoot());
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.
     * IProgressMonitor)
     */
    @Override
    protected IStatus run(IProgressMonitor monitor) {
      boolean more = false;
      do {
        IModel[] changedModels = null;
        synchronized (models) {
          changedModels = models.toArray(new IModel[models.size()]);
          models.clear();
        }
        fireModelProviderEvent(
            new ModelProviderEvent(
                this,
                IModelProviderEvent.MODELS_CHANGED,
                new IModel[0],
                new IModel[0],
                changedModels));
        synchronized (models) {
          more = !models.isEmpty();
        }
      } while (more);
      return Status.OK_STATUS;
    }

    /**
     * Queues more projects/containers.
     *
     * @param projects
     * @param containers
     */
    void add(IModel model) {
      synchronized (models) {
        models.add(model);
      }
    }
  }

  private ModelsChangedJob modelChangeJob = new ModelsChangedJob();
}
