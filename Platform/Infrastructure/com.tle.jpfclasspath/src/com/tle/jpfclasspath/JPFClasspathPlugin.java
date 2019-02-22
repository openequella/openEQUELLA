package com.tle.jpfclasspath;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.QualifiedName;
import org.osgi.framework.BundleContext;

@SuppressWarnings("nls")
public class JPFClasspathPlugin extends Plugin {
  public static final String PLUGIN_ID = "com.tle.jpfclasspath";
  public static final IPath CONTAINER_PATH = new Path(PLUGIN_ID + ".CONTAINER");
  public static final String MARKER_ID = PLUGIN_ID + ".jpfProblem";
  public static final String PREF_REGISTRY_NAME = "registryName";
  public static final String PREF_PARENT_REGISTRIES = "parentRegistries";
  public static final QualifiedName TOUCH_PROJECT = new QualifiedName(PLUGIN_ID, "TOUCHED");

  private static JPFClasspathPlugin inst;
  private JPFPluginRebuilder fPluginRebuilder;

  public JPFClasspathPlugin() {
    inst = this;
  }

  public static JPFClasspathPlugin getDefault() {
    return inst;
  }

  @Override
  public void start(BundleContext context) throws Exception {
    super.start(context);
    fPluginRebuilder = new JPFPluginRebuilder();
    fPluginRebuilder.start();
  }

  @Override
  public void stop(BundleContext context) throws Exception {
    super.stop(context);
    fPluginRebuilder.stop();
  }
}
