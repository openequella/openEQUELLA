package io.github.openequella.dashboard

import com.tle.webtests.framework.TestInstitution
import io.github.openequella.pages.dashboard.PortletType
import io.github.openequella.pages.dashboard.portlets.PortletFactory.MyResources
import org.testng.Assert.assertTrue
import org.testng.annotations.Test
import testng.annotation.NewUIOnly

object MyResourceCategories {
  val PUBLISHED        = "Published"        -> "Published"
  val DRAFTS           = "Drafts"           -> "Drafts"
  val SCRAPBOOK        = "Scrapbook"        -> "Scrapbook"
  val MODERATION_QUEUE = "Moderation queue" -> "Moderation+queue"
  val ARCHIVE          = "Archive"          -> "Archive"

  val ALL_CATEGORIES = Map(PUBLISHED, DRAFTS, SCRAPBOOK, MODERATION_QUEUE, ARCHIVE)

  val MOD_QUEUE_SUB_CATEGORIES = Map(
    "In moderation" -> "%22status%22%3A%5B%22MODERATING%22%5D",
    "Under review"  -> "%22status%22%3A%5B%22REVIEW%22%5D",
    "Rejected"      -> "%22status%22%3A%5B%22REJECTED%22%5D"
  )

  val ALL_RESOURCES = "All+resources"
}

@NewUIOnly
@TestInstitution("vanilla")
class MyResourcesPortletTest extends AbstractPortletTest {

  @Test(description = "Should navigate to correct filtered views when clicking categories")
  def testMyResourcesPortletInteractions(): Unit = {
    val portletName = context.getFullName("My Resources")

    // Create the portlet
    dashboardPage.createPortlet(PortletType.MyResources, portletName)
    assertTrue(dashboardPage.hasPortlet(portletName))

    val myResources = dashboardPage.getPortlet(MyResources, portletName)

    // Select each top-level category and verify the URL filter.
    MyResourceCategories.ALL_CATEGORIES.foreach { case (itemName, urlFragmentExpected) =>
      assertTrue(myResources.hasCategory(itemName))
      myResources.clickCategory(itemName)
      assertUrlContains(urlFragmentExpected)
      loadDashboardPage()
    }

    // Select each moderation sub-category and verify the status filter in the URL
    MyResourceCategories.MOD_QUEUE_SUB_CATEGORIES.foreach { case (subCat, urlFragmentExpected) =>
      assertTrue(myResources.hasCategory(subCat))
      myResources.clickCategory(subCat)
      assertUrlContains(urlFragmentExpected)
      loadDashboardPage()
    }

    // Ensure the 'Show all' button navigates to the 'All resources' view.
    myResources.clickShowAll()
    assertUrlContains(MyResourceCategories.ALL_RESOURCES)
  }

  private def assertUrlContains(expected: String): Unit = {
    assertTrue(context.getDriver.getCurrentUrl.contains(expected))
  }
}
