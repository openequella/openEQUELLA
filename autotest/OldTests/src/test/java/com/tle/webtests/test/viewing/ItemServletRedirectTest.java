package com.tle.webtests.test.viewing;

import static org.testng.Assert.assertEquals;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.ErrorPage;
import com.tle.webtests.test.AbstractCleanupAutoTest;
import org.testng.annotations.Test;

@TestInstitution("fiveo")
public class ItemServletRedirectTest extends AbstractCleanupAutoTest {

  private static final String ITEM_URL = "items/268fbf77-ffdd-4731-a474-77c63c6aea62/0?frogs=frogs";
  private static final String BADITEM_URL = "items/268fbf77-ffdd-4731-a474-77c63c6aea62/";
  private static final String BADITEM_URL2 = "items/268fbf77-ffdd-4731-a474-77c63c6aea6/0/";
  private static final String BADITEM_URL3 =
      "items/268fbf77-ffdd-4731-a474-77c63c6aea62/0/?attachment.uuid=broken";

  @Override
  protected boolean isCleanupItems() {
    return false;
  }

  @Test
  public void testRedirects() {
    context.getDriver().get(context.getBaseUrl() + ITEM_URL);
    assertEquals(
        context.getDriver().getCurrentUrl(),
        context.getBaseUrl() + "items/268fbf77-ffdd-4731-a474-77c63c6aea62/1/?frogs=frogs");
  }

  @Test
  public void testErrors() {
    context.getDriver().get(context.getBaseUrl() + BADITEM_URL);
    assertEquals(
        new ErrorPage(context, true).get().getMainErrorMessage(false), "An error occurred");
    context.getDriver().get(context.getBaseUrl() + BADITEM_URL2);
    assertEquals(
        new ErrorPage(context, true).get().getMainErrorMessage(false), "Resource not found");
    context.getDriver().get(context.getBaseUrl() + BADITEM_URL3);
    assertEquals(
        new ErrorPage(context, true).get().getMainErrorMessage(false), "Resource not found");
  }
}
