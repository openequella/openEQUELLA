package com.tle.jpfclasspath.model;

import java.util.List;

public interface IResolvedPlugin {
  IPluginModel getPluginModel();

  List<ResolvedImport> getImports();

  List<IResolvedPlugin> getFragments();

  IResolvedPlugin getHostPlugin();

  String getRegistryName();
}
