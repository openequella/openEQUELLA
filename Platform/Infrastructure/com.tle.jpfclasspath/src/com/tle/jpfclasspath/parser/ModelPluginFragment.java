package com.tle.jpfclasspath.parser;

public final class ModelPluginFragment extends ModelPluginManifest {
  private String pluginId;
  private Version pluginVersion;
  private MatchingRule matchingRule = MatchingRule.COMPATIBLE;

  ModelPluginFragment() {
    // no-op
  }

  public MatchingRule getMatchingRule() {
    return matchingRule;
  }

  void setMatchingRule(final MatchingRule value) {
    matchingRule = value;
  }

  public String getPluginId() {
    return pluginId;
  }

  void setPluginId(final String value) {
    pluginId = value;
  }

  public Version getPluginVersion() {
    return pluginVersion;
  }

  void setPluginVersion(final String value) {
    pluginVersion = Version.parse(value);
  }
}
