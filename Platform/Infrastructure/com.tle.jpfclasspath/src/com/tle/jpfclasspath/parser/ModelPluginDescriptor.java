package com.tle.jpfclasspath.parser;

public final class ModelPluginDescriptor extends ModelPluginManifest {
  private String className;

  ModelPluginDescriptor() {
    // no-op
  }

  String getClassName() {
    return className;
  }

  void setClassName(final String value) {
    className = value;
  }
}
