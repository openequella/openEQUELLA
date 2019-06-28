package com.tle.webtests.test.admin;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.CustomLinksEditPage;
import com.tle.webtests.pageobject.CustomLinksPage;
import com.tle.webtests.test.AbstractSessionTest;
import com.tle.webtests.test.files.Attachments;
import java.net.URL;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

@TestInstitution("flakey")
public class CustomLinksTest extends AbstractSessionTest {
  @Override
  protected void prepareBrowserSession() {
    logon("TLE_ADMINISTRATOR", testConfig.getAdminPassword());
  }

  @DataProvider(name = "links", parallel = false)
  public Object[][] links() {
    return new Object[][] {
      {"Google", "http://www.google.com/", true}, {"Yahoo", "http://www.yahoo.com/", false}
    };
  }

  @Test(dataProvider = "links")
  public void testAddLink(String name, String url, boolean icon) {
    CustomLinksPage clp = new CustomLinksPage(context).load();
    CustomLinksEditPage newLink = clp.newLink();
    newLink.setName(name);
    newLink.setUrl(url);
    if (icon) {
      newLink.downloadIcon();
    }
    clp = newLink.save();
    assertTrue(clp.waitForLink(name, url, icon));
  }

  @Test(
      dependsOnMethods = {"testAddLink"},
      dataProvider = "links")
  public void testRenameLink(String name, String url, boolean icon) {
    String newName = name + "2";
    String newUrl = url + "2";
    CustomLinksPage clp = new CustomLinksPage(context).load();
    CustomLinksEditPage clep = clp.editLink(name, url);
    clep.setName(newName);
    clep.setUrl(newUrl);
    clp = clep.save();
    assertTrue(clp.waitForLink(newName, newUrl, icon));
    clep = clp.editLink(newName, newUrl);
    clep.setName(name);
    clep.setUrl(url);
    clep.save();
  }

  @Test(
      dependsOnMethods = {"testAddLink"},
      dataProvider = "links")
  public void testUploadIcon(String name, String url, boolean icon) {
    CustomLinksPage clp = new CustomLinksPage(context).load();

    CustomLinksEditPage clep = clp.editLink(name, url);
    URL upIcon = Attachments.get("favicon.ico");
    clep.uploadIcon(upIcon, icon); // overwriting blank icon / downloaded icon with one on disk
    clp = clep.save();
    assertTrue(clp.waitForLink(name, url, true)); // testing that a link exists with an icon
    clep = clp.editLink(name, url);
    clep.deleteIcon();
    if (icon) {
      clep.downloadIcon();
    }
    clep.save();
  }

  @Test(
      dependsOnMethods = {"testUploadIcon"},
      dataProvider = "links")
  public void testDeleteLink(String name, String url, boolean icon) {
    CustomLinksPage clp = new CustomLinksPage(context).load();
    clp.deleteLink(name, url);
    logon("TLE_ADMINISTRATOR", testConfig.getAdminPassword());
    clp = new CustomLinksPage(context).load();
    assertFalse(clp.linkExistsOnMenu(name, url, icon));
  }

  @Override
  protected void cleanupAfterClass() throws Exception {
    logon("TLE_ADMINISTRATOR", testConfig.getAdminPassword());
    CustomLinksPage clp = new CustomLinksPage(context).load();
    if (clp.linkExists("Google", "http://www.google.com/")) {
      clp.deleteLink("Google", "http://www.google.com/");
    }
    if (clp.linkExists("Yahoo", "http://www.yahoo.com/")) {
      clp.deleteLink("Yahoo", "http://www.yahoo.com/");
    }
    super.cleanupAfterClass();
  }
}
