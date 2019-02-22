package com.tle.jpfclasspath.model;

import com.tle.jpfclasspath.JPFClasspathPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;

public class JPFProject {
  public static IPath MANIFEST_PATH = new Path("plugin-jpf.xml"); // $NON-NLS-1$

  public static IFile getManifest(IProject project) {
    return project.getFile(MANIFEST_PATH);
  }

  public static IEclipsePreferences getPreferences(IProject project) {
    return new ProjectScope(project).getNode(JPFClasspathPlugin.PLUGIN_ID);
  }

  public static IFolder getResourcesFolder(IProject project) {
    return project.getFolder("resources");
  }
}
