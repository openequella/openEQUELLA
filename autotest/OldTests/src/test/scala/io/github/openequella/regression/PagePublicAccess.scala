package io.github.openequella.regression

import com.tle.webtests.framework.TestInstitution
import com.tle.webtests.pageobject.searching.SearchPage
import com.tle.webtests.test.AbstractCleanupAutoTest
import io.github.openequella.pages.dashboard.DashboardPage
import io.github.openequella.pages.hierarchy.BrowseHierarchiesPage
import io.github.openequella.pages.search.NewSearchPage
import org.testng.Assert.assertTrue
import org.testng.annotations.Test
import testng.annotation.NewUIOnly

/** Tests to verify that front end page permissions are correctly checked based on the page ACL
  * settings.
  */
@NewUIOnly
@TestInstitution("publicaccess")
class PagePublicAccess extends AbstractCleanupAutoTest {
  @Test(description = "Guest user should be able to access dashboard page from the new URL.")
  def accessDashboardFromNewUrl(): Unit = {
    val dashboard = new DashboardPage(context).load
    assertTrue(dashboard.isLoaded)
  }

  @Test(description = "Guest user should be able to access dashboard page from the legacy URL.")
  def accessDashboardFromLegacyUrl(): Unit = {
    val dashboard = new DashboardPage(context).loadFromLegacyEntry
    assertTrue(dashboard.isLoaded)
  }

  @Test(description = "Guest user should be able to access hierarchy page from the new URL.")
  def accessHierarchyFromNewUrl(): Unit = {
    val hierarchy = new BrowseHierarchiesPage(context).load
    assertTrue(hierarchy.isLoaded)
  }

  @Test(description = "Guest user should be able to access hierarchy page from the legacy URL.")
  def accessHierarchyFromLegacyUrl(): Unit = {
    val hierarchy = new BrowseHierarchiesPage(context).loadFromLegacyEntry
    assertTrue(hierarchy.isLoaded)
  }

  @Test(description = "Guest user should be able to access search page from the new URL.")
  def accessSearchFromNewUrl(): Unit = {
    val searchPage = new NewSearchPage(context).load
    assertTrue(searchPage.isLoaded)
  }

  @Test(description = "Guest user should be able to access search page from the legacy URL.")
  def accessSearchFromLegacyUrl(): Unit = {
    val searchPage = new SearchPage(context).load
    assertTrue(searchPage.isLoaded)
  }
}
