package io.github.openequella.dashboard

import com.tle.webtests.framework.TestInstitution
import io.github.openequella.pages.dashboard.PortletType
import io.github.openequella.pages.dashboard.portlets.PortletFactory.MyResources
import org.testng.Assert.assertTrue
import org.testng.annotations.Test
import testng.annotation.NewUIOnly

@NewUIOnly
@TestInstitution("vanilla")
class MyResourcesPortletTest extends AbstractPortletTest {

  @Test(description = "Should be able to see categories and navigate to them")
  def testMyResourcesPortletInteractions(): Unit = {
    val portletName = context.getFullName("My Resources")

    // Create the portlet
    dashboardPage.createPortlet(PortletType.MyResources, portletName)
    assertTrue(dashboardPage.hasPortlet(portletName))

    val myResources = dashboardPage.getPortlet(MyResources, portletName)

    // Select each top-level category and verify the URL filter.
    val categories = Map(
      "Published"        -> "Published",
      "Drafts"           -> "Drafts",
      "Scrapbook"        -> "Scrapbook",
      "Moderation queue" -> "Moderation+queue",
      "Archive"          -> "Archive"
    )

    categories.foreach { case (itemName, urlType) =>
      assertTrue(myResources.hasCategory(itemName))
      myResources.clickCategory(itemName)
      assertTrue(context.getDriver.getCurrentUrl.contains(s"myResourcesType=$urlType"))
      loadDashboardPage()
    }

    // Select each moderation sub-category and verify the status filter in the URL
    val modQueueSubCategories = Map(
      "In moderation" -> "%22status%22%3A%5B%22MODERATING%22%5D",
      "Under review"  -> "%22status%22%3A%5B%22REVIEW%22%5D",
      "Rejected"      -> "%22status%22%3A%5B%22REJECTED%22%5D"
    )

    modQueueSubCategories.foreach { case (subCat, urlExpected) =>
      assertTrue(myResources.hasCategory(subCat))
      myResources.clickCategory(subCat)
      assertTrue(context.getDriver.getCurrentUrl.contains(urlExpected))
      loadDashboardPage()
    }

    // Ensure the 'Show all' button navigates to the 'All resources' view.
    myResources.clickShowAll()
    assertTrue(context.getDriver.getCurrentUrl.contains("myResourcesType=All+resources"))
  }
}
