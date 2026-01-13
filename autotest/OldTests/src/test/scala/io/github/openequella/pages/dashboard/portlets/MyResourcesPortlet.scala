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

  /** Locator for a My Resources category based on its display name.
    *
    * @param resourceName
    *   Display name of the category.
    * @return
    *   A `By` selector targeting the category element via its `data-testid`.
    */
  private def resourceBy(resourceName: String): By =
    By.cssSelector(s"[data-testid='my-resources-category-$resourceName']")

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

  /** Click the "Show all" button to navigate to the unfiltered view of all MyResources across all
    * categories.
    */
  def clickShowAll(): Unit = {
    driver.findElement(By.cssSelector("[data-testid='my-resources-show-all-button']")).click()
  }
}
