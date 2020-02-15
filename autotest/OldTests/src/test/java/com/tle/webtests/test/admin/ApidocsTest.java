package com.tle.webtests.test.admin;

import static org.testng.Assert.assertEquals;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.AccessDeniedErrorPage;
import com.tle.webtests.pageobject.ApidocsPage;
import com.tle.webtests.pageobject.LoginPage;
import com.tle.webtests.test.AbstractSessionTest;
import org.testng.annotations.Test;

@TestInstitution("fiveo")
public class ApidocsTest extends AbstractSessionTest {

  @Test
  public void testLoginNoAccess() {
    new LoginPage(context).load().login("AutoTest", "automated");

    context.getDriver().get(context.getBaseUrl() + ApidocsPage.getUrl());
    AccessDeniedErrorPage error = new AccessDeniedErrorPage(context);
    assertAccessDenied(error);
  }

  @Test
  public void testDefaultNoAccess() {
    context.getDriver().get(context.getBaseUrl() + ApidocsPage.getUrl());
    AccessDeniedErrorPage error = new AccessDeniedErrorPage(context);
    assertAccessDenied(error);
  }

  @Test
  public void testLoginWithAccess() {
    new LoginPage(context).load().login("AutoTestWithViewApidocs", "automated");
    loadAndAssertSuccess();
  }

  @Test
  public void testTleAdminAccess() {
    new LoginPage(context).load().login("TLE_ADMINISTRATOR", this.testConfig.getAdminPassword());
    loadAndAssertSuccess();
  }

  private void assertAccessDenied(AccessDeniedErrorPage error) {
    assertEquals(error.getMainErrorMessage(), "Access denied");
    assertEquals(
        error.getSubErrorMessage(), "Sorry you do not have access to view the page you requested.");
    assertEquals(
        error.getDenied(),
        "You do not have the required privileges to access this object [VIEW_APIDOCS]");
  }

  private void loadAndAssertSuccess() {
    ApidocsPage ap = new ApidocsPage(context);
    ap.load();
    assertEquals(ap.getMainHeader(), "openEQUELLA REST API");
  }
}
