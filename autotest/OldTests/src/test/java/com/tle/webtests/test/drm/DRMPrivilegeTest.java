package com.tle.webtests.test.drm;

import static org.testng.Assert.assertEquals;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.ErrorPage;
import com.tle.webtests.pageobject.LoginPage;
import com.tle.webtests.pageobject.viewitem.DRMAgreementPage;
import com.tle.webtests.test.AbstractTest;
import org.testng.annotations.Test;

@TestInstitution("vanilla")
public class DRMPrivilegeTest extends AbstractTest {
  private static final String DITEM_URL = "items/90ce974a-1534-d81d-f910-c0201bce2db1/1/";
  private static final String DPITEM_URL = "items/0c2b2d62-19a2-ce71-4d59-f852c33e6413/1/";
  private static final String VITEM_URL = "items/63810b7d-2534-d4ff-1d2f-ddae660f596d/1/";
  private static final String VPITEM_URL = "items/e98420d8-f010-513b-9aa5-74ece19fc0ec/1/";
  private static final String PAGE_HTML = "page.html";
  private static final String OLD_UI = "?old=true";

  private void assertDenied() {
    assertEquals(
        new ErrorPage(context, new ErrorPage(context).usingNewUI()).get().getMainErrorMessage(),
        "Access denied");
  }

  private void assertAgreement() {
    new DRMAgreementPage(context).get();
  }

  private void assertLogin() {
    new LoginPage(context).get();
  }

  private void openPage(String url) {
    context.getDriver().get(context.getBaseUrl() + url);
  }

  @Test
  public void testWithDiscover() {
    LoginPage loginPage = new LoginPage(context).load();
    loginPage.login("DRMPriv", "``````");
    openPage(DITEM_URL + OLD_UI);
    assertAgreement();
    openPage(DITEM_URL + PAGE_HTML + OLD_UI);
    assertDenied();
    loginPage.logout();
    openPage(DITEM_URL + OLD_UI);
    assertAgreement();
    openPage(DITEM_URL + PAGE_HTML + OLD_UI);
    loginPage.checkLoaded();
  }

  @Test
  public void testWithDiscoverNoPreview() {
    LoginPage loginPage = new LoginPage(context).load();
    loginPage.login("DRMPriv", "``````");
    openPage(DPITEM_URL + OLD_UI);
    assertAgreement();
    openPage(DPITEM_URL + PAGE_HTML + OLD_UI);
    assertDenied();
    loginPage.logout();
    openPage(DPITEM_URL + OLD_UI);
    assertLogin();
    openPage(DPITEM_URL + PAGE_HTML + OLD_UI);
    assertLogin();
  }

  @Test
  public void testWithView() {
    LoginPage loginPage = new LoginPage(context).load();
    loginPage.login("DRMPriv", "``````");
    openPage(VITEM_URL + OLD_UI);
    assertAgreement();
    openPage(VITEM_URL + PAGE_HTML + OLD_UI);
    assertAgreement();
    loginPage.logout();
    openPage(VITEM_URL + OLD_UI);
    assertAgreement();
    openPage(VITEM_URL + PAGE_HTML + OLD_UI);
    assertAgreement();
  }

  @Test
  public void testWithViewNoPreview() {
    LoginPage loginPage = new LoginPage(context).load();
    loginPage.login("DRMPriv", "``````");
    openPage(VPITEM_URL + OLD_UI);
    assertAgreement();
    openPage(VPITEM_URL + PAGE_HTML + OLD_UI);
    assertAgreement();
    loginPage.logout();
    openPage(VPITEM_URL + OLD_UI);
    assertLogin();
    openPage(VPITEM_URL + PAGE_HTML + OLD_UI);
    assertLogin();
  }

  @Test
  public void testWithNothing() {
    LoginPage loginPage = new LoginPage(context).load();
    loginPage.login("DRMNoPriv", "``````");
    openPage(VITEM_URL + "?old=true");
    assertDenied();
    openPage(VITEM_URL + PAGE_HTML + OLD_UI);
    assertDenied();
  }
}
