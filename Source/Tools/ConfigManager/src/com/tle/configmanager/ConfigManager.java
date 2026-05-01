package com.tle.configmanager;

// Author: Andrew Gibb

public final class ConfigManager {

  private ConfigManager() {
    throw new UnsupportedOperationException();
  }

  @SuppressWarnings("unused")
  public static void main(String[] args) {
    new ConfigLauncher();
  }
}
