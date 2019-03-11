package com.tle.webtests.test.searching.manage;

import static org.testng.Assert.assertEquals;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.myresources.MyResourcesPage;
import com.tle.webtests.pageobject.searching.ItemAdminPage;
import com.tle.webtests.pageobject.searching.ItemListPage;
import com.tle.webtests.test.AbstractCleanupAutoTest;
import com.tle.webtests.test.files.Attachments;
import org.testng.annotations.Test;

/**
 * @author Dustin
 * @see Redmine #7476
 */
@TestInstitution("manageresources")
public class ManageResourcesNoScrapbookTest extends AbstractCleanupAutoTest {
  @Test
  public void testNoScrap() {
    final String itemName = context.getFullName("Scrappy");

    logon("autotest", "automated");
    contributeScrapbookItem(itemName);
    ItemAdminPage itemAdmin = new ItemAdminPage(context).load();
    ItemListPage list = itemAdmin.exactQuery(itemName);
    assertEquals(list.getResults().size(), 0);
    itemAdmin.setWithinCollection("My content");
    list = itemAdmin.exactQuery(itemName);
    assertEquals(list.getResults().size(), 1);
    logout();
  }

  private MyResourcesPage contributeScrapbookItem(String fullName) {
    MyResourcesPage myResourcesPage = new MyResourcesPage(context, "scrapbook").load();
    myResourcesPage
        .uploadFile(Attachments.get("avatar.png"), fullName, "file")
        .exactQuery(fullName);
    return myResourcesPage;
  }

  @Override
  protected void cleanupAfterClass() throws Exception {
    logon("autotest", "automated");
    ItemAdminPage itemAdmin = new ItemAdminPage(context).load();
    itemAdmin.setWithinCollection("My content");
    itemAdmin.search(context.getNamePrefix());
    itemAdmin.bulk().deleteAll();
    itemAdmin.setWithinCollection("My content");
    ItemListPage list = itemAdmin.search(context.getNamePrefix());
    itemAdmin.bulk().purgeAll();
    logout();
  }
}
