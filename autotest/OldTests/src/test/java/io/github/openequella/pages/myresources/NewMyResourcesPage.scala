package io.github.openequella.pages.myresources

import com.tle.webtests.framework.PageContext
import io.github.openequella.pages.search.AbstractSearchPage
import org.openqa.selenium.By

import scala.jdk.CollectionConverters._

class NewMyResourcesPage(context: PageContext)
    extends AbstractSearchPage[NewMyResourcesPage](context) {
  loadedBy = By.xpath("//h5[text()='My Resources']")

  /** Gets the currently selected text from the My Resources dropdown. Finds the element with
    * role='combobox' inside the MyResourcesSelector panel.
    */
  def getMyResourcesSelectorValue: String =
    getRefineControl("MyResourcesSelector")
      .findElement(By.cssSelector("div[role='combobox']"))
      .getText

  /** Gets a list of all currently selected Statuses (chips) from the Status selector. Finds all
    * elements with class 'MuiChip-label' inside the StatusSelector panel.
    */
  def getStatusSelectorValues: List[String] =
    getRefineControl("StatusSelector")
      .findElements(By.className("MuiChip-label"))
      .asScala
      .map(_.getText)
      .toList
}
