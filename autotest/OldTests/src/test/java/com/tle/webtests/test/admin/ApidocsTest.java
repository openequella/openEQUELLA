package com.tle.webtests.test.admin;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.ApidocsJsonPage;
import com.tle.webtests.pageobject.ApidocsPage;
import com.tle.webtests.pageobject.ErrorPage;
import com.tle.webtests.pageobject.LoginPage;
import com.tle.webtests.test.AbstractSessionTest;
import org.testng.annotations.Test;

@TestInstitution("fiveo")
public class ApidocsTest extends AbstractSessionTest {

  @Test
  public void testLoginNoAccess() {
    new LoginPage(context).load().login("AutoTest", "automated");

    context.getDriver().get(context.getBaseUrl() + ApidocsPage.getUrl());
    ErrorPage error = new ErrorPage(context);
    assertAccessDenied(error);
  }

  @Test
  public void testLoginNoAccessJson() {
    new LoginPage(context).load().login("AutoTest", "automated");
    loadAndAssertAccessDeniedJson();
  }

  @Test
  public void testDefaultNoAccess() {
    context.getDriver().get(context.getBaseUrl() + ApidocsPage.getUrl());
    ErrorPage error = new ErrorPage(context);
    assertAccessDenied(error);
  }

  @Test
  public void testDefaultNoAccessJson() {
    loadAndAssertAccessDeniedJson();
  }

  @Test
  public void testTleAdminAccess() {
    new LoginPage(context).load().login("TLE_ADMINISTRATOR", this.testConfig.getAdminPassword());
    loadAndAssertSuccess();
  }

  @Test
  public void testTleAdminAccessJson() {
    new LoginPage(context).load().login("TLE_ADMINISTRATOR", this.testConfig.getAdminPassword());
    loadAndAssertSuccessJson();
  }

  @Test
  public void testLoginWithAccess() {
    new LoginPage(context).load().login("AutoTestWithViewApidocs", "automated");
    loadAndAssertSuccess();
  }

  @Test
  public void testLoginWithAccessJson() {
    new LoginPage(context).load().login("AutoTestWithViewApidocs", "automated");
    loadAndAssertSuccessJson();
  }

  private void assertAccessDenied(ErrorPage error) {
    assertEquals(error.getMainErrorMessage(), testConfig.isNewUI() ? "Forbidden" : "Access denied");
    assertEquals(
        error.getSubErrorMessage(),
        testConfig.isNewUI()
            ? "403 : Forbidden"
            : "Sorry you do not have access to view the page you requested.");
    assertEquals(
        error.getDenied(),
        "You do not have the required privileges to access this object [VIEW_APIDOCS]");
  }

  private void loadAndAssertSuccess() {
    ApidocsPage ap = new ApidocsPage(context);
    ap.load();
    assertEquals(ap.getMainHeader(), "openEQUELLA REST API");
  }

  private void loadAndAssertSuccessJson() {
    ApidocsJsonPage page = new ApidocsJsonPage(context);
    page.load();

    assertTrue(page.getLoadedElement().getText().contains("tags"), "JSON should contain 'tags'");
    assertTrue(
        page.getLoadedElement().getText().contains("swagger"), "JSON should contain 'swagger'");
    assertTrue(
        page.getLoadedElement().getText().contains("parameters"),
        "JSON should contain 'parameters'");
  }

  private void loadAndAssertAccessDeniedJson() {
    ApidocsJsonPage page = new ApidocsJsonPage(context);
    page.load();

    assertTrue(
        page.getLoadedElement().getText().contains("\"code\":403"),
        "JSON should contain error code");
    assertTrue(
        page.getLoadedElement().getText().contains("\"error\":\"Forbidden\""),
        "JSON should contain error status");
    assertTrue(
        page.getLoadedElement()
            .getText()
            .contains(
                "\"error_description\":\"You do not have the required privileges to access this object [VIEW_APIDOCS]\""),
        "JSON should contain error description");
  }
}
