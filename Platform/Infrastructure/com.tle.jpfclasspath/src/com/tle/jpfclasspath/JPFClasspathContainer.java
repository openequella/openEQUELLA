/**
 * ***************************************************************************** Copyright (c) 2000,
 * 2013 IBM Corporation and others. All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html Contributors: IBM
 * Corporation - initial API and implementation
 * *****************************************************************************
 */
package com.tle.jpfclasspath;

import com.tle.jpfclasspath.model.IModel;
import com.tle.jpfclasspath.model.IPluginModel;
import com.tle.jpfclasspath.model.IResolvedPlugin;
import com.tle.jpfclasspath.model.JPFPluginModelManager;
import com.tle.jpfclasspath.model.ResolvedImport;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

@SuppressWarnings("nls")
public class JPFClasspathContainer implements IClasspathContainer {
  private IPluginModel fModel;
  private IClasspathEntry[] fEntries;

  /** Constructor for RequiredPluginsClasspathContainer. */
  public JPFClasspathContainer(IPluginModel model) {
    fModel = model;
  }

  @Override
  public int getKind() {
    return K_APPLICATION;
  }

  @Override
  public IPath getPath() {
    return JPFClasspathPlugin.CONTAINER_PATH;
  }

  @Override
  public String getDescription() {
    return "JPF Plugin libraries";
  }

  @Override
  public IClasspathEntry[] getClasspathEntries() {
    if (fModel == null) {
      return new IClasspathEntry[0];
    }

    if (fEntries == null) {
      fEntries = computePluginEntries();
    }
    return fEntries;
  }

  private IClasspathEntry[] computePluginEntries() {
    List<IClasspathEntry> entries = new ArrayList<>();
    IResolvedPlugin resolvedPlugin = JPFPluginModelManager.instance().getResolvedPlugin(fModel);
    if (resolvedPlugin == null) {
      return new IClasspathEntry[0];
    }
    Set<IModel> seen = new HashSet<>();
    IResolvedPlugin hostPlugin = resolvedPlugin.getHostPlugin();
    if (hostPlugin != null) {
      addImports(hostPlugin, entries, true, true, false, seen);
    }
    addImports(resolvedPlugin, entries, true, false, true, seen);
    return entries.toArray(new IClasspathEntry[entries.size()]);
  }

  private void addImports(
      IResolvedPlugin resolvedPlugin,
      List<IClasspathEntry> entries,
      boolean allImports,
      boolean includeSelf,
      boolean includeFragments,
      Set<IModel> seen) {
    IPluginModel model = resolvedPlugin.getPluginModel();
    if (model == null) {
      return;
    }
    if (!seen.add(model)) {
      return;
    }
    seen.add(model);
    if (includeSelf) {
      entries.addAll(model.createClasspathEntries());
    }
    List<ResolvedImport> imports = resolvedPlugin.getImports();
    for (ResolvedImport resolvedImport : imports) {
      IResolvedPlugin resolvedImportPlugin = resolvedImport.getResolved();
      if (allImports || resolvedImport.getPrerequisite().isExported()) {
        addImports(resolvedImportPlugin, entries, false, true, true, seen);
      }
    }
    if (includeFragments) {
      for (IResolvedPlugin fragment : resolvedPlugin.getFragments()) {
        addImports(fragment, entries, allImports, true, false, seen);
      }
    }
  }

  public static void addToProject(IJavaProject javaProject) {
    try {
      Set<IClasspathEntry> entries = new LinkedHashSet<>();
      entries.addAll(Arrays.asList(javaProject.getRawClasspath()));
      entries.add(JavaCore.newContainerEntry(JPFClasspathPlugin.CONTAINER_PATH));
      javaProject.setRawClasspath(entries.toArray(new IClasspathEntry[entries.size()]), null);
    } catch (JavaModelException e) {
      JPFClasspathLog.logError(e);
    }
  }

  public static void removeFromProject(IJavaProject javaProject) {
    try {
      Set<IClasspathEntry> entries = new LinkedHashSet<>();
      entries.addAll(Arrays.asList(javaProject.getRawClasspath()));
      if (entries.remove(JavaCore.newContainerEntry(JPFClasspathPlugin.CONTAINER_PATH))) {
        javaProject.setRawClasspath(entries.toArray(new IClasspathEntry[entries.size()]), null);
      }
    } catch (JavaModelException e) {
      JPFClasspathLog.logError(e);
    }
  }
}
