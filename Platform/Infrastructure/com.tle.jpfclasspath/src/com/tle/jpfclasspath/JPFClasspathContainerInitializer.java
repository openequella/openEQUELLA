package com.tle.jpfclasspath;

import com.tle.jpfclasspath.model.IPluginModel;
import com.tle.jpfclasspath.model.JPFPluginModelManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ClasspathContainerInitializer;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

public class JPFClasspathContainerInitializer extends ClasspathContainerInitializer {
  @Override
  public void initialize(IPath containerPath, IJavaProject javaProject) throws CoreException {
    IProject project = javaProject.getProject();
    IPluginModel model = JPFPluginModelManager.instance().findModel(project);
    JavaCore.setClasspathContainer(
        containerPath,
        new IJavaProject[] {javaProject},
        new IClasspathContainer[] {new JPFClasspathContainer(model)},
        null);
  }
}
