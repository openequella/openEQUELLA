package com.tle.jpfclasspath.model;

import org.eclipse.core.resources.IResource;

public interface IModel {
  void reload();

  IResource getUnderlyingResource();
}
