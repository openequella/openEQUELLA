package io.github.openequella.pages.hierarchy

import com.tle.webtests.framework.PageContext
import com.tle.webtests.pageobject.AbstractPage
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.{By, WebElement}

class BrowseHierarchiesPage(context: PageContext)
    extends AbstractPage[BrowseHierarchiesPage](context) {
  val TITLE = "Browse hierarchies"
  loadedBy = By.xpath("//h5[text()='" + TITLE + "']")
  val hierarchyPanel = new HierarchyPanel(context)

  override protected def loadUrl(): Unit = {
    driver.get(context.getBaseUrl + "page/hierarchies")
  }

  override def findLoadedElement: WebElement =
    waiter.until(
      ExpectedConditions.presenceOfElementLocated(By.xpath("//ul[@aria-label='View hierarchy']"))
    )
}
