package com.tle.webtests.test;

import com.google.common.base.Throwables;
import com.tle.webtests.framework.StandardDriverPool;
import com.tle.webtests.framework.WebDriverCheckout;
import com.tle.webtests.framework.WebDriverPool;
import org.openqa.selenium.WebDriver;

public class PreparingDriverPool implements WebDriverPool {
  private ThreadLocal<PerThreadData> checkoutLocal = new ThreadLocal<PerThreadData>();
  private StandardDriverPool driverPool;
  private AbstractTest test;

  public PreparingDriverPool(AbstractTest abstractTest, StandardDriverPool driverPool) {
    this.test = abstractTest;
    this.driverPool = driverPool;
  }

  @Override
  public WebDriver getDriver() {
    PerThreadData perThread = getPerThread();
    if (perThread.checkedout == null) {
      WebDriverCheckout newDriver;
      try {
        newDriver = driverPool.getDriver(perThread.preferred, test.getClass());
        if (newDriver != perThread.preferred) {
          perThread.invalid = true;
        }
        perThread.checkedout = newDriver;
      } catch (Exception e) {
        throw Throwables.propagate(e);
      }
    }
    if (perThread.invalid) {
      perThread.invalid = false;
      test.browserInvalid(perThread.checkedout.getDriver());
    }
    return perThread.checkedout.getDriver();
  }

  private PerThreadData getPerThread() {
    PerThreadData data = checkoutLocal.get();
    if (data == null) {
      data = new PerThreadData();
      checkoutLocal.set(data);
    }
    return data;
  }

  @Override
  public void releaseDriver(WebDriver driver) {
    PerThreadData perThread = getPerThread();
    WebDriverCheckout checkedout = perThread.checkedout;
    WebDriver existingDriver = checkedout.getDriver();
    if (existingDriver != driver) {
      throw new RuntimeException("Try to release wrong driver");
    }
    perThread.preferred = checkedout;
    perThread.checkedout = null;
    driverPool.releaseDriver(checkedout);
  }

  private static class PerThreadData {
    WebDriverCheckout checkedout;
    WebDriverCheckout preferred;
    boolean invalid;
  }

  public void invalidateSession() {
    getPerThread().invalid = true;
  }
}
