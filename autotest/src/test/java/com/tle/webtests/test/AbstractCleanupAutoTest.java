package com.tle.webtests.test;

public abstract class AbstractCleanupAutoTest extends AbstractCleanupTest {
  @Override
  protected void prepareBrowserSession() {
    logon();
  }
}
