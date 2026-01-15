package io.github.openequella.dashboard

import com.tle.webtests.framework.TestInstitution
import io.github.openequella.pages.dashboard.PortletType
import io.github.openequella.pages.dashboard.portlets.PortletFactory.MyResources
import org.testng.Assert.assertTrue
import org.testng.annotations.Test
import testng.annotation.NewUIOnly

object MyResourceCategories {
  val PUBLISHED        = "Published"
  val DRAFTS           = "Drafts"
  val SCRAPBOOK        = "Scrapbook"
  val MODERATION_QUEUE = "Moderation queue"
  val ARCHIVE          = "Archive"

  val ALL_CATEGORIES: Set[String] = Set(PUBLISHED, DRAFTS, SCRAPBOOK, MODERATION_QUEUE, ARCHIVE)

  val MOD_QUEUE_SUB_CATEGORIES: Set[String] = Set(
    "In moderation",
    "Under review",
    "Rejected"
  )
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
    MyResourceCategories.ALL_CATEGORIES.foreach { case (itemName) =>
      assertTrue(myResources.hasCategory(itemName))
      myResources.clickCategory(itemName)
      loadDashboardPage()
    }

    // Select each moderation sub-category and verify the status filter in the URL
    MyResourceCategories.MOD_QUEUE_SUB_CATEGORIES.foreach { case (subCat) =>
      assertTrue(myResources.hasCategory(subCat))
      myResources.clickCategory(subCat)
      loadDashboardPage()
    }

    // Ensure the 'Show all' button navigates to the 'All resources' view.
    myResources.clickShowAll()
  }
}
