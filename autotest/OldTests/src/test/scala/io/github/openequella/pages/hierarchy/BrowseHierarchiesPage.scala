package io.github.openequella.pages.hierarchy

import com.tle.webtests.framework.PageContext
import com.tle.webtests.pageobject.AbstractPage
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.{By, WebElement}

class BrowseHierarchiesPage(context: PageContext)
    extends AbstractPage[BrowseHierarchiesPage](context) {
  val TITLE                = "Browse hierarchies"
  val NO_RESULT            = "No hierarchies available"
  val VIEW_HIERARCHY_LABEL = "View hierarchy"

  loadedBy = By.xpath("//h5[text()='" + TITLE + "']")
  val hierarchyPanel = new HierarchyPanel(context)

  override protected def loadUrl(): Unit = loadPath("page/hierarchies")

  override protected def loadLegacyUrl(): Unit = loadPath("hierarchy.do")

  override def findLoadedElement: WebElement =
    waiter.until(
      ExpectedConditions.presenceOfElementLocated(
        By.xpath(s"//ul[@aria-label='$VIEW_HIERARCHY_LABEL'] | //div[text()='$NO_RESULT']")
      )
    )
}
