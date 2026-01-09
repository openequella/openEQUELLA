package io.github.openequella.pages.dashboard.portlets

import com.tle.webtests.framework.PageContext
import org.openqa.selenium.By
import org.openqa.selenium.support.ui.ExpectedConditions

class MyResourcesPortlet(context: PageContext, name: String)
    extends GenericPortlet[MyResourcesPortlet](context, name) {

  /** Check if a category with the given name exists in the portlet.
    *
    * @param name
    *   Name of the category (e.g., "Published", "Drafts")
    * @return
    *   true if the category is present, false otherwise
    */
  def hasCategory(name: String): Boolean = isPresent(resourceBy(name))

  private def resourceBy(resourceName: String): By = By.xpath(
    s"$portletXpath//a[contains(@class, 'MuiListItemButton-root') and .//span[text()='$resourceName']]"
  )

  /** Click on a specific category link within the portlet. Waits for the element to be clickable
    * before interacting.
    *
    * @param name
    *   Name of the category to click
    */
  def clickCategory(name: String): Unit = {
    val element = waiter.until(ExpectedConditions.elementToBeClickable(resourceBy(name)))
    element.click()
  }

  /** Click the "Show all" button to navigate to the full list of resources.
    */
  def clickShowAll(): Unit = {
    driver.findElement(By.xpath(s"$portletXpath//a[text()='Show all']")).click()
  }
}
