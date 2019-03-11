package com.tle.webtests.test.cal;

import static org.testng.Assert.assertTrue;

import com.tle.webtests.pageobject.cal.ManageActivationsPage;
import java.lang.reflect.Method;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

public class AbstractActivationsTest extends AbstractCALTest {
  private final String subPrefix;

  public AbstractActivationsTest(String subPrefix) {
    super();
    this.subPrefix = subPrefix;
  }

  @AfterMethod(alwaysRun = true)
  public void deleteActivations() {
    logon("caladmin", "``````");
    ManageActivationsPage activations = new ManageActivationsPage(context).load();
    activations.search('"' + context.getFullName("") + '"');
    if (activations.hasResults()) {
      assertTrue(activations.bulk().commandAll("delete"));
    }
  }

  @Override
  protected void prepareBrowserSession() {
    logon("caladmin", "``````");
  }

  @Override
  @BeforeMethod
  public void setupSubcontext(Method testMethod) {
    context.setSubPrefix(subPrefix);
  }
}
