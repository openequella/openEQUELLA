package com.tle.webtests.test.admin;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.ApidocsJsonPage;
import com.tle.webtests.pageobject.ApidocsPage;
import com.tle.webtests.pageobject.ErrorPage;
import com.tle.webtests.test.AbstractSessionTest;
import org.testng.annotations.Test;

@TestInstitution("fiveo")
public class ApidocsTest extends AbstractSessionTest {
  @Test
  public void testLoginNoAccess() {
    logout(context);
    logon(context, "AutoTest", "automated");

    context.getDriver().get(context.getBaseUrl() + ApidocsPage.getUrl());
    ErrorPage error = new ErrorPage(context);
    assertAccessDenied(error);
  }

  @Test
  public void testLoginNoAccessJson() {
    logout(context);
    logon(context, "AutoTest", "automated");
    loadAndAssertAccessDeniedJson();
  }

  @Test
  public void testDefaultNoAccess() {
    logout(context);
    context.getDriver().get(context.getBaseUrl() + ApidocsPage.getUrl());
    ErrorPage error = new ErrorPage(context);
    assertAccessDenied(error);
  }

  @Test
  public void testDefaultNoAccessJson() {
    logout(context);
    loadAndAssertAccessDeniedJson();
  }

  @Test
  public void testTleAdminAccess() {
    logout(context);
    logon(context, "TLE_ADMINISTRATOR", this.testConfig.getAdminPassword());
    loadAndAssertSuccess();
  }

  //  This has passed locally, but fails at times with the following response.  Commenting out for
  // now.
  //	[http://localhost:8080/fiveo/api/swagger.json] > {"swagger":"2.0","basePath":"/fiveo/api"}
  //  @Test
  //  public void testTleAdminAccessJson() {
  //    logout(context);
  //    logon(context, "TLE_ADMINISTRATOR", this.testConfig.getAdminPassword());
  //    loadAndAssertSuccessJson();
  //  }

  @Test
  public void testLoginWithAccess() {
    logout(context);
    logon(context, "AutoTestWithViewApidocs", "automated");
    loadAndAssertSuccess();
  }

  //  This has passed locally, but fails at times with the following response.  Commenting out for
  // now.
  //	[http://localhost:8080/fiveo/api/swagger.json] > {"swagger":"2.0","basePath":"/fiveo/api"}
  //    @Test
  //    public void testLoginWithAccessJson() {
  //		logout(context);
  //	logon(context,"AutoTestWithViewApidocs", "automated");
  //      loadAndAssertSuccessJson();
  //    }

  private void assertAccessDenied(ErrorPage error) {
    if (!testConfig.isNewUI()) {
      assertEquals(error.getMainErrorMessage(false), "Access denied");
    }
    assertEquals(
        error.getSubErrorMessage(testConfig.isNewUI()),
        testConfig.isNewUI()
            ? "403 : Forbidden"
            : "Sorry you do not have access to view the page you requested.");
    assertEquals(
        error.getDenied(),
        "You do not have the required privileges to access this object [VIEW_APIDOCS]");
  }

  private void loadAndAssertSuccess() {
    ApidocsPage page = new ApidocsPage(context);
    page.load();
    final String respText = "[" + page.getFullUrl() + "] > " + page.getLoadedElement().getText();

    assertEquals(
        page.getMainHeader(), "openEQUELLA REST API", "HTML instead contained: [" + respText + "]");
  }

  private void loadAndAssertSuccessJson() {
    ApidocsJsonPage page = new ApidocsJsonPage(context);
    page.load();

    final String respText = "[" + page.getFullUrl() + "] > " + page.getLoadedElement().getText();

    assertTrue(
        respText.contains("tags"),
        "JSON should contain 'tags', instead it contained: [" + respText + "]");
    assertTrue(
        respText.contains("swagger"),
        "JSON should contain 'swagger', instead it contained: [" + respText + "]");
    assertTrue(
        respText.contains("parameters"),
        "JSON should contain 'parameters', instead it contained: [" + respText + "]");
  }

  private void loadAndAssertAccessDeniedJson() {
    ApidocsJsonPage page = new ApidocsJsonPage(context);
    page.load();
    final String respText = "[" + page.getFullUrl() + "] > " + page.getLoadedElement().getText();
    assertTrue(
        respText.contains("\"code\":403"),
        "JSON should contain error code, instead it contained: [" + respText + "]");
    assertTrue(
        respText.contains("\"error\":\"Forbidden\""),
        "JSON should contain error status, instead it contained: [" + respText + "]");
    assertTrue(
        respText.contains(
            "\"error_description\":\"You do not have the required privileges to access this object [VIEW_APIDOCS]\""),
        "JSON should contain error description, instead it contained: [" + respText + "]");
  }
}
