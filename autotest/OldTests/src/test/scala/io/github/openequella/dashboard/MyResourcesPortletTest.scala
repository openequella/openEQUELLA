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

  @Test(description =
    "Verifies the existence and clickability of MyResources categories and sub-categories"
  )
  def testMyResourcesPortletInteractions(): Unit = {
    val portletName = context.getFullName("My Resources")

    // Create the portlet
    dashboardPage.createPortlet(PortletType.MyResources, portletName)
    assertTrue(dashboardPage.hasPortlet(portletName))

    val myResources = dashboardPage.getPortlet(MyResources, portletName)

    // Verify each top-level category presence and interaction.
    MyResourceCategories.ALL_CATEGORIES.foreach { case (itemName) =>
      assertTrue(myResources.hasCategory(itemName))
      myResources.clickCategory(itemName)
      loadDashboardPage()
    }

    // Verify each moderation sub-category presence and interaction.
    MyResourceCategories.MOD_QUEUE_SUB_CATEGORIES.foreach { case (subCat) =>
      assertTrue(myResources.hasCategory(subCat))
      myResources.clickCategory(subCat)
      loadDashboardPage()
    }

    // Ensure the 'Show all' button exists and is interactable.
    myResources.clickShowAll()
  }
}
