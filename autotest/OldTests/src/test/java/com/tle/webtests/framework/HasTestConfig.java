package com.tle.webtests.framework;

public interface HasTestConfig {

  TestConfig getTestConfig();

  PageContext getContext();

  void invalidateSession();
}
