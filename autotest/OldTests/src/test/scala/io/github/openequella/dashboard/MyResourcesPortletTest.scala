package io.github.openequella.dashboard

import com.tle.webtests.framework.TestInstitution
import io.github.openequella.pages.dashboard.PortletType
import io.github.openequella.pages.dashboard.portlets.MyResourcesPortlet
import io.github.openequella.pages.dashboard.portlets.PortletFactory.MyResources
import io.github.openequella.pages.myresources.NewMyResourcesPage
import org.testng.Assert.{assertEquals, assertFalse, assertTrue}
import org.testng.annotations.{BeforeClass, Test}
import testng.annotation.NewUIOnly

// Object for My Resources categories
object MyResourceCategories {
  val PUBLISHED        = "Published"
  val DRAFTS           = "Drafts"
  val SCRAPBOOK        = "Scrapbook"
  val MODERATION_QUEUE = "Moderation queue"
  val ARCHIVE          = "Archive"
  val ALL_RESOURCES    = "All resources"

  val CATEGORY_COUNTS: Map[String, Int] = Map(
    PUBLISHED        -> 47,
    DRAFTS           -> 6,
    SCRAPBOOK        -> 9,
    MODERATION_QUEUE -> 9,
    ARCHIVE          -> 3
  )
}

// Object for 'Moderation queue' sub-categories
object ModerationQueueSubCategories {
  val IN_MODERATION = "In moderation"
  val UNDER_REVIEW  = "Under review"
  val REJECTED      = "Rejected"

  val SUBCATEGORY_COUNTS: Map[String, Int] = Map(
    IN_MODERATION -> 9,
    UNDER_REVIEW  -> 0,
    REJECTED      -> 0
  )
}

@NewUIOnly
@TestInstitution("rest")
class MyResourcesPortletTest extends AbstractPortletTest {
  // Override to use the user (AutoTest) for 'rest' institution, instead of portlettest1
  override protected def loginWithPortletAccount(): Unit = logon()

  var portletName: String             = _
  var myResources: MyResourcesPortlet = _

  @BeforeClass
  def setupClass(): Unit = {
    loginWithPortletAccount()
    portletName = context.getFullName("My Resources")
    createMyResourcesPortlet(portletName)
  }

  @Override
  override def cleanupAfterClass(): Unit = {
    logon("test_myresources", "``````")
    cleanupPortlets()

    super.cleanupAfterClass()
  }

  @Test(description =
    "Verifies the existence of MyResources categories, sub-categories, and 'SHOW ALL' button"
  )
  def testContentPresence(): Unit = {
    myResources = getMyResourcesPortlet(portletName)

    // Verify each top-level category and their count presence and interaction.
    MyResourceCategories.CATEGORY_COUNTS.foreach { case (itemName, expectedCount) =>
      assertTrue(myResources.hasCategory(itemName), s"Category $itemName should be present")

      val actualCount = myResources.getCategoryCount(itemName)
      assertEquals(
        actualCount,
        expectedCount,
        s"Count for category '$itemName' did not match expected value."
      )
    }

    // Verify each moderation sub-category and their count presence and interaction.
    ModerationQueueSubCategories.SUBCATEGORY_COUNTS.foreach { case (subCat, expectedCount) =>
      assertTrue(myResources.hasCategory(subCat), s"Sub-category $subCat should be present")

      val actualCount = myResources.getCategoryCount(subCat)
      assertEquals(
        actualCount,
        expectedCount,
        s"Count for sub-category '$subCat' did not match expected value."
      )
    }

    // Check if the "Show All" button exists
    assertTrue(myResources.hasShowAllButton, "The 'Show All' button should be present")
  }

  @Test(description =
    "Verifies navigation behavior for top-level categories, sub-categories, and 'Show All' button"
  )
  def testNavigations(): Unit = {
    // Test navigation for top-level categories
    MyResourceCategories.CATEGORY_COUNTS.keys.foreach { itemName =>
      myResources = getMyResourcesPortlet(portletName)
      myResources.clickCategory(itemName)
      val myResourcesPage = loadMyResourcesPage

      // Verify My Resources selector matches the category clicked
      assertEquals(myResourcesPage.getMyResourcesSelectorValue, itemName)
      loadDashboardPage()
    }

    val statusMapping: Map[String, String] = Map(
      ModerationQueueSubCategories.IN_MODERATION -> "MODERATING",
      ModerationQueueSubCategories.UNDER_REVIEW  -> "REVIEW",
      ModerationQueueSubCategories.REJECTED      -> "REJECTED"
    )

    // Test navigation for moderation sub-categories
    ModerationQueueSubCategories.SUBCATEGORY_COUNTS.keys.foreach { subCat =>
      myResources = getMyResourcesPortlet(portletName)
      myResources.clickCategory(subCat)
      val myResourcesPage = loadMyResourcesPage

      assertEquals(
        myResourcesPage.getMyResourcesSelectorValue,
        MyResourceCategories.MODERATION_QUEUE
      )
      myResourcesPage.expandRefineControlPanel()

      // Verify Status selector contains the correct status chip
      val expectedStatus  = statusMapping.getOrElse(subCat, subCat.toUpperCase)
      val currentStatuses = myResourcesPage.getStatusSelectorValues
      assertTrue(
        currentStatuses.contains(expectedStatus),
        s"Status selector values $currentStatuses did not contain expected status '$expectedStatus'"
      )
      loadDashboardPage()
    }

    // Click the "Show All" button and verify navigation
    myResources = getMyResourcesPortlet(portletName)
    myResources.clickShowAll()
    val myResourcesPage = loadMyResourcesPage
    assertEquals(myResourcesPage.getMyResourcesSelectorValue, MyResourceCategories.ALL_RESOURCES)
  }

  @Test(description =
    "Verifies Scrapbook category is excluded when access to Scrapbook is disabled"
  )
  def testRestrictedUserScrapbookAccess(): Unit = {
    logon("test_myresources", "``````")

    val restrictedPortletName = context.getFullName("Scrapbook disabled")
    createMyResourcesPortlet(restrictedPortletName)
    val restrictedPortlet = getMyResourcesPortlet(restrictedPortletName)

    assertFalse(
      restrictedPortlet.hasCategory(MyResourceCategories.SCRAPBOOK),
      "Scrapbook category should not be visible for user with disabled scrapbook"
    )
  }

  private def createMyResourcesPortlet(name: String): Unit = {
    loadDashboardPage()
    dashboardPage.createPortlet(PortletType.MyResources, name)
  }

  private def getMyResourcesPortlet(name: String): MyResourcesPortlet =
    dashboardPage.getPortlet(MyResources, name)

  private def loadMyResourcesPage: NewMyResourcesPage = new NewMyResourcesPage(context).get()
}
