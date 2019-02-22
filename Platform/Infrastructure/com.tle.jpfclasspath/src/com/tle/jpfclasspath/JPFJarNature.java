package com.tle.jpfclasspath;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;

public class JPFJarNature implements IProjectNature {
  public static final String NATURE_ID = JPFClasspathPlugin.PLUGIN_ID + ".jpfjars";

  private IProject project;

  @Override
  public void configure() throws CoreException {
    // nothing
  }

  @Override
  public void deconfigure() throws CoreException {
    // nothing
  }

  @Override
  public IProject getProject() {
    return project;
  }

  @Override
  public void setProject(IProject project) {
    this.project = project;
  }

  public static void addNature(IProjectDescription description) {
    Set<String> newIds = new LinkedHashSet<>();
    newIds.addAll(Arrays.asList(description.getNatureIds()));
    if (newIds.add(NATURE_ID)) {
      description.setNatureIds(newIds.toArray(new String[newIds.size()]));
    }
  }
}
