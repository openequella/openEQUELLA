package io.github.openequella.hierarchy

import com.tle.webtests.framework.TestInstitution
import com.tle.webtests.pageobject.HomePage
import com.tle.webtests.pageobject.portal.MenuSection
import com.tle.webtests.test.AbstractCleanupAutoTest
import io.github.openequella.pages.hierarchy.{BrowseHierarchiesPage, HierarchyPage}
import org.testng.Assert.{assertFalse, assertTrue}
import org.testng.annotations.Test
import testng.annotation.NewUIOnly

/**
  * New UI tests for virtual hierarchy.
  */
@TestInstitution("hierarchy")
class VirtualHierarchyTest extends AbstractCleanupAutoTest {
  private val TOPIC_1 = "dynamic_topic 1"
  // Topic 1
  private val TOPIC_1_UUID = "666446e4-542a-4ee3-8668-bca5fabf4f3b:dG9waWMgMQ=="
  private val TOPIC_2      = "dynamic_topic 2"
  private val TOPIC_3      = "dynamic_topic 3"
  // Topic 3
  private val TOPIC_3_UUID = "666446e4-542a-4ee3-8668-bca5fabf4f3b:dG9waWMgMw=="

  private val TESTING_ITEM = "Testing item 3"

  private def openHierarchyPage(hierarchyName: String, compoundUuid: String): HierarchyPage =
    new HierarchyPage(context, hierarchyName, compoundUuid).load()

  // Add key resource to hierarchy to prepare for the test.
  private def prepareKeyResource(hierarchyName: String,
                                 hierarchyUuid: String,
                                 itemName: String): Unit = {
    val hierarchyPage = openHierarchyPage(hierarchyName, hierarchyUuid)
    hierarchyPage.addKeyResourceFromResultList(itemName)
    assertTrue(hierarchyPage.hasKeyResource(itemName))
  }

  // TODO: Test virtual hierarchy which name has comma. OEQ-2013.
  @NewUIOnly
  @Test(description = "User should be able to see all virtual topics from menu.")
  def checkGeneratedHierarchyMenu(): Unit = {
    val homePage = new HomePage(context).load
    assertTrue(homePage.isTopicTagVisible(TOPIC_1))
    assertTrue(homePage.isTopicTagVisible(TOPIC_2))
    assertTrue(homePage.isTopicTagVisible(TOPIC_3))
  }

  @NewUIOnly
  @Test(description = "User should be able to access virtual hierarchy page from the menu.")
  def accessVirtualHierarchyFromMenu(): Unit = {
    new HomePage(context).load
    val menus         = new MenuSection(context).get
    val hierarchyPage = menus.clickMenu(TOPIC_1, new HierarchyPage(context, TOPIC_1, TOPIC_1_UUID))
    assertTrue(hierarchyPage.isLoaded)
  }

  @Test(description = "Test the link of virtual hierarchy in hierarchy panel.")
  def accessVirtualHierarchyFromBrowsePage(): Unit = {
    val browsePage    = new BrowseHierarchiesPage(context).load
    val hierarchyPage = browsePage.hierarchyPanel.clickHierarchy(TOPIC_1, TOPIC_1_UUID)
    assertTrue(hierarchyPage.isLoaded)
  }

  @NewUIOnly
  @Test(
    description =
      "User should be able to add virtual key resource by clicking the pin icon in item list."
  )
  def addVirtualKeyResourceFromResultList(): Unit = {
    val hierarchyPage = openHierarchyPage(TOPIC_3, TOPIC_3_UUID)
    assertFalse(hierarchyPage.isItemPinIconHighlighted(TESTING_ITEM))

    hierarchyPage.addKeyResourceFromResultList(TESTING_ITEM)

    assertTrue(hierarchyPage.hasKeyResource(TESTING_ITEM))
    assertTrue(hierarchyPage.isItemPinIconHighlighted(TESTING_ITEM))

    // clean key resource
    hierarchyPage.removeKeyResourceFromSearchResult(TESTING_ITEM)
  }

  @NewUIOnly
  @Test(
    description =
      "User should be able to remove virtual key resource by clicking the pin icon in item list.",
  )
  def removeVirtualKeyResourceFromResultList(): Unit = {
    val hierarchyPage = openHierarchyPage(TOPIC_3, TOPIC_3_UUID)
    prepareKeyResource(TOPIC_3, TOPIC_3_UUID, TESTING_ITEM)

    hierarchyPage.removeKeyResourceFromSearchResult(TESTING_ITEM)

    assertFalse(hierarchyPage.hasKeyResource(TESTING_ITEM))
    assertFalse(hierarchyPage.isItemPinIconHighlighted(TESTING_ITEM))
  }

  @NewUIOnly
  @Test(
    description =
      "User should be able to remove virtual key resource by clicking the pin icon in the Key Resource Panel.")
  def removeVirtualKeyResourceFromKeyResourcePanel(): Unit = {
    val hierarchyPage = openHierarchyPage(TOPIC_3, TOPIC_3_UUID)
    prepareKeyResource(TOPIC_3, TOPIC_3_UUID, TESTING_ITEM)

    hierarchyPage.removeKeyResourceFromKeyResourcePanel(TESTING_ITEM)

    assertFalse(hierarchyPage.hasKeyResource(TESTING_ITEM))
    assertFalse(hierarchyPage.isItemPinIconHighlighted(TESTING_ITEM))
  }
}
