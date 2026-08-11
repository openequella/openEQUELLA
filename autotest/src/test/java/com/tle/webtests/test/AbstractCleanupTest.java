package com.tle.webtests.test;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.LoginPage;
import com.tle.webtests.pageobject.searching.ItemAdminPage;
import com.tle.webtests.pageobject.searching.ItemListPage;
import java.lang.reflect.Method;
import org.testng.annotations.BeforeMethod;

public class AbstractCleanupTest extends AbstractSessionTest {
  protected String namePrefix;
  private String usernameForDelete;
  private String passwordForDelete;

  public AbstractCleanupTest() {
    namePrefix = getClass().getSimpleName();
  }

  public AbstractCleanupTest(String namePrefix) {
    this.namePrefix = namePrefix;
  }

  @Override
  protected String getNamePrefix() {
    return namePrefix;
  }

  public void setDeleteCredentials(String username, String password) {
    this.usernameForDelete = username;
    this.passwordForDelete = password;
  }

  @Override
  protected void cleanupAfterClass() throws Exception {
    if (isCleanupItems() && namePrefix != null) {
      if (usernameForDelete != null) {
        deleteItemsWithPrefix(context, usernameForDelete, passwordForDelete, namePrefix);
      } else if (lastUsername != null) {
        deleteItemsWithPrefix(context, lastUsername, lastPassword, namePrefix, notice);
      } else {

        new Error("Need to login to cleanup:" + getClass()).printStackTrace();
      }
    }
  }

  public static void deleteItemsWithPrefix(
      PageContext context, String username, String password, String prefix) {
    deleteItemsWithPrefix(context, username, password, prefix, false);
  }

  public static void deleteItemsWithPrefix(
      PageContext context, String username, String password, String prefix, boolean notice) {
    if (notice) {
      new LoginPage(context).load().loginWithNotice(username, password).acceptNotice();
    } else {
      new LoginPage(context).load().login(username, password);
    }
    ItemAdminPage filterListPage = new ItemAdminPage(context).load();
    ItemListPage filterResults = filterListPage.all().search(prefix);
    if (filterResults.isResultsAvailable()) {
      filterListPage.bulk().deleteAll();
      ItemListPage postDeleteItemListPage = filterListPage.get().search(prefix);
      if (postDeleteItemListPage.isResultsAvailable()) {
        filterListPage.bulk().purgeAll();
      }
    }
  }

  protected boolean isCleanupItems() {
    return true;
  }

  @BeforeMethod
  public void setupSubcontext(Method method) {
    context.setSubPrefix(method.getName());
  }
}
