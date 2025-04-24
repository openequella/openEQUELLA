package com.tle.webtests.test;

import integtester.IntegTester;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import scala.Function0;
import scala.runtime.BoxedUnit;

/**
 * Abstract class to be used when the IntegTester is required in a test case. Since this class
 * extends {@link AbstractCleanupTest}, override {@link AbstractCleanupTest#isCleanupItems()} to
 * skip the final clean up wherever applicable.
 */
public abstract class AbstractIntegrationTest extends AbstractCleanupTest {

  private Function0<BoxedUnit> stopIntegServer;

  public AbstractIntegrationTest() {
    super();
  }

  public AbstractIntegrationTest(String namePrefix) {
    super(namePrefix);
  }

  @BeforeSuite
  public void startIntegServer() {
    stopIntegServer = IntegTester.start();
  }

  @AfterSuite
  public void stopIntegTester() {
    stopIntegServer.apply();
  }
}
