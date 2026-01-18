package io.github.openequella.pages.dashboard.portlets

import com.tle.webtests.framework.PageContext
import org.openqa.selenium.By
import org.openqa.selenium.support.ui.ExpectedConditions

class MyResourcesPortlet(context: PageContext, name: String)
    extends GenericPortlet[MyResourcesPortlet](context, name) {
  private val showAllButtonBy = By.cssSelector("[role='link'][aria-label='Show all']")

  /** Locator for a My Resources category based on its display name.
    *
    * @param resourceName
    *   Display name of the category.
    * @return
    *   A `By` selector targeting the category element via its `aria-label`.
    */
  private def resourceBy(resourceName: String): By =
    By.cssSelector(s"a[aria-label^='$resourceName']")

  /** Check if a category with the given name exists in the portlet.
    *
    * @param name
    *   Name of the category (e.g., "Published", "Drafts")
    * @return
    *   true if the category is present, false otherwise
    */
  def hasCategory(name: String): Boolean = isPresent(resourceBy(name))

  /** Retrieve the item count for a specific category.
    *
    * @param name
    *   Name of the category (e.g., "Published")
    * @return
    *   The count as an Integer. Returns 0 if no count chip is visible.
    */
  def getCategoryCount(name: String): Int = {
    checkLoadedElement()
    val categoryElement = getLoadedElement.findElement(resourceBy(name))
    val chips           = categoryElement.findElements(By.cssSelector(".MuiChip-label"))

    // If list is empty, count is 0. Otherwise, parse the text.
    if (chips.isEmpty) 0 else chips.get(0).getText.trim.toInt
  }

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

  /** Check if the "Show All" button is present in the portlet. */
  def hasShowAllButton: Boolean = isPresent(showAllButtonBy)

  /** Click the "Show all" button to navigate to the unfiltered view of all MyResources across all
    * categories.
    */
  def clickShowAll(): Unit = {
    driver.findElement(showAllButtonBy).click()
  }
}
