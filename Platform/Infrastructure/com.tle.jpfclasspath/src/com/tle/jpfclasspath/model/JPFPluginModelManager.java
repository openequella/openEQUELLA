package com.tle.jpfclasspath.model;

import com.tle.jpfclasspath.JPFClasspathContainer;
import com.tle.jpfclasspath.JPFClasspathLog;
import com.tle.jpfclasspath.JPFClasspathPlugin;
import com.tle.jpfclasspath.JPFPluginRebuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

public class JPFPluginModelManager implements IModelProviderListener {
  private static JPFPluginModelManager instance;
  private WorkspaceJarModelManager jarManager;
  private WorkspacePluginModelManager projectManager;
  private Map<IRepoModel, JPFPluginRegistry> repoRegistries = new HashMap<>();
  private Map<IPluginModel, JPFPluginRegistry> primaryRegistry = new HashMap<>();
  private Map<String, JPFPluginRegistry> registries = new HashMap<>();

  private JPFPluginModelManager() {
    jarManager = new WorkspaceJarModelManager();
    projectManager = new WorkspacePluginModelManager();
    jarManager.addModelProviderListener(this);
    projectManager.addModelProviderListener(this);
    PluginUpdateTracker tracker = new PluginUpdateTracker();
    IRepoModel[] repoModels = jarManager.getRepoModels();
    for (IRepoModel repoModel : repoModels) {
      addRepoModel(repoModel, tracker);
      List<? extends IPluginModel> jarModels = repoModel.getJarModels();
      for (IPluginModel pluginModel : jarModels) {
        addPluginModel(pluginModel, tracker);
      }
    }
    IPluginModel[] models = projectManager.getPluginModels();
    for (IPluginModel pluginModel : models) {
      addPluginModel(pluginModel, tracker);
    }
  }

  private void addPluginModel(IPluginModel pluginModel, PluginUpdateTracker tracker) {
    String registryName = pluginModel.getRegistryName();
    if (registryName.equals(IPluginModel.DEFAULT_REGISTRY)) {
      registryName = "External Dependencies";
    }
    JPFPluginRegistry registry = getOrCreateRegistry(registryName);
    primaryRegistry.put(pluginModel, registry);
    registry.addModel(pluginModel, tracker);
  }

  private boolean removePluginModel(IPluginModel pluginModel, PluginUpdateTracker tracker) {
    JPFPluginRegistry registry = primaryRegistry.remove(pluginModel);
    if (registry != null) {
      registry.removeModel(pluginModel, tracker);
      return true;
    }
    return false;
  }

  private void addRepoModel(IRepoModel repoModel, PluginUpdateTracker tracker) {
    JPFPluginRegistry registry = getOrCreateRegistry(repoModel.getName());
    repoRegistries.put(repoModel, registry);
    registry.setParentRepos(repoModel.getParentRepos());
    Set<String> parentRepos = repoModel.getParentRepos();
    for (String parent : parentRepos) {
      JPFPluginRegistry parentReg = getOrCreateRegistry(parent);
      parentReg.addChildRegistry(registry, tracker);
    }
  }

  private void removeRepo(IRepoModel model, PluginUpdateTracker tracker) {
    JPFPluginRegistry registry = repoRegistries.remove(model);
    if (registry != null) {
      Set<String> parentRepos = registry.getParentRepos();
      for (String parent : parentRepos) {
        JPFPluginRegistry parentRepo = getOrCreateRegistry(parent);
        parentRepo.removeChildRegistry(registry, tracker);
      }
    }
  }

  private JPFPluginRegistry getOrCreateRegistry(String name) {
    JPFPluginRegistry registry = registries.get(name);
    if (registry == null) {
      registry = new JPFPluginRegistry(name);
      registries.put(name, registry);
    }
    return registry;
  }

  public static synchronized JPFPluginModelManager instance() {
    if (instance == null) {
      instance = new JPFPluginModelManager();
    }
    return instance;
  }

  @Override
  public synchronized void modelsChanged(IModelProviderEvent event) {
    PluginUpdateTracker tracker = new PluginUpdateTracker();
    debugChange(event);
    IModel[] removed = event.getRemovedModels();
    if (removed.length > 0) {
      for (IModel model : removed) {
        if (model instanceof IPluginModel) {
          removePluginModel((IPluginModel) model, tracker);
        } else if (model instanceof IRepoModel) {
          removeRepo((IRepoModel) model, tracker);
        }
      }
    }
    IModel[] added = event.getAddedModels();
    if (added.length > 0) {
      for (IModel model : added) {
        if (model instanceof IPluginModel) {
          addPluginModel((IPluginModel) model, tracker);
        } else if (model instanceof IRepoModel) {
          addRepoModel((IRepoModel) model, tracker);
        }
      }
    }
    IModel[] changed = event.getChangedModels();
    boolean changesFlag = changed.length > 0;
    if (changesFlag) {
      for (IModel model : changed) {
        if (model instanceof IPluginModel) {
          IPluginModel pluginModel = (IPluginModel) model;
          if (removePluginModel(pluginModel, tracker)) {
            addPluginModel(pluginModel, tracker);
          }
        } else if (model instanceof IRepoModel) {
          repoChanged((IRepoModel) model, tracker);
        }
      }
    }
    JPFClasspathLog.logInfo("Classpath updates:" + tracker.getChanges());
    JPFPluginRebuilder.instance().pluginsChanged(tracker.getChanges());
    List<IJavaProject> projects = new ArrayList<>();
    List<IClasspathContainer> containers = new ArrayList<>();
    Set<IPluginModel> changes = tracker.getChanges();
    for (IPluginModel changedModel : changes) {
      IJavaProject javaProject = changedModel.getJavaProject();
      if (javaProject != null) {
        projects.add(javaProject);
        containers.add(new JPFClasspathContainer(changedModel));
      }
    }
    IJavaProject[] affectedProjects = projects.toArray(new IJavaProject[projects.size()]);
    IClasspathContainer[] affectedContainers =
        containers.toArray(new IClasspathContainer[containers.size()]);
    if (affectedProjects.length > 0) {
      if (changesFlag) {
        fUpdateJob.add(affectedProjects, affectedContainers);
        fUpdateJob.schedule();
      } else {
        try {
          JavaCore.setClasspathContainer(
              JPFClasspathPlugin.CONTAINER_PATH, affectedProjects, affectedContainers, null);
        } catch (JavaModelException e) {
          JPFClasspathLog.logError(e);
        }
      }
    }
  }

  private void debugChange(IModelProviderEvent event) {
    if (event.getRemovedModels().length > 0) {
      JPFClasspathLog.logInfo("Removed:" + Arrays.asList(event.getRemovedModels()));
    }
    if (event.getAddedModels().length > 0) {
      JPFClasspathLog.logInfo("Added:" + Arrays.asList(event.getAddedModels()));
    }
    if (event.getChangedModels().length > 0) {
      JPFClasspathLog.logInfo("Changed:" + Arrays.asList(event.getChangedModels()));
    }
  }

  private void repoChanged(IRepoModel model, PluginUpdateTracker tracker) {
    Set<String> newParents = model.getParentRepos();
    JPFPluginRegistry registry = repoRegistries.get(model);
    if (registry == null) {
      return;
    }
    Set<String> parentRepos = registry.getParentRepos();
    if (!parentRepos.equals(newParents)) {
      registry.setParentRepos(newParents);
      for (String parent : parentRepos) {
        JPFPluginRegistry parentRepo = getOrCreateRegistry(parent);
        parentRepo.removeChildRegistry(registry, tracker);
      }
      for (String parent : newParents) {
        JPFPluginRegistry parentRepo = getOrCreateRegistry(parent);
        parentRepo.addChildRegistry(registry, tracker);
      }
    }
  }

  public IPluginModel findModel(IProject project) {
    return projectManager.getPluginModel(project);
  }

  public IPluginModel findModel(IFile jarFile) {
    return jarManager.getPluginModel(jarFile);
  }

  public IResolvedPlugin getResolvedPlugin(IPluginModel model) {
    JPFPluginRegistry registry = primaryRegistry.get(model);
    if (registry == null) {
      return null;
    }
    return registry.getResolvedPlugin(model);
  }

  class UpdateClasspathsJob extends Job {

    private List<IJavaProject> fProjects = new ArrayList<IJavaProject>();
    private List<IClasspathContainer> fContainers = new ArrayList<IClasspathContainer>();

    /** Constructs a new job. */
    public UpdateClasspathsJob() {
      super("Update JPF classpaths");
      // The job is given a workspace lock so other jobs can't run on a
      // stale classpath (bug 354993)
      setRule(ResourcesPlugin.getWorkspace().getRoot());
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.
     * IProgressMonitor)
     */
    @Override
    protected IStatus run(IProgressMonitor monitor) {
      try {
        boolean more = false;
        do {
          IJavaProject[] projects = null;
          IClasspathContainer[] containers = null;
          synchronized (fProjects) {
            projects = fProjects.toArray(new IJavaProject[fProjects.size()]);
            containers = fContainers.toArray(new IClasspathContainer[fContainers.size()]);
            fProjects.clear();
            fContainers.clear();
          }
          JavaCore.setClasspathContainer(
              JPFClasspathPlugin.CONTAINER_PATH, projects, containers, monitor);
          synchronized (fProjects) {
            more = !fProjects.isEmpty();
          }
        } while (more);

      } catch (JavaModelException e) {
        return e.getStatus();
      }
      return Status.OK_STATUS;
    }

    /**
     * Queues more projects/containers.
     *
     * @param projects
     * @param containers
     */
    void add(IJavaProject[] projects, IClasspathContainer[] containers) {
      synchronized (fProjects) {
        for (int i = 0; i < containers.length; i++) {
          fProjects.add(projects[i]);
          fContainers.add(containers[i]);
        }
      }
    }
  }

  /** Job used to update class path containers. */
  private UpdateClasspathsJob fUpdateJob = new UpdateClasspathsJob();
}
