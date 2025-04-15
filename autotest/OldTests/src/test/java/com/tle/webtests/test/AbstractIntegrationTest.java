package com.tle.webtests.test;

import integtester.IntegTester;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
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

  @BeforeClass
  public void startIntegServer() {
    stopIntegServer = IntegTester.start();
  }

  @AfterClass
  public void stopIntegTester() {
    stopIntegServer.apply();
  }
}
