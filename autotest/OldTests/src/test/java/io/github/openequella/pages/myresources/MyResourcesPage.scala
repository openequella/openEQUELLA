package io.github.openequella.pages.myresources

import com.tle.webtests.framework.PageContext
import io.github.openequella.pages.search.AbstractSearchPage
import org.openqa.selenium.By

class MyResourcesPage(context: PageContext) extends AbstractSearchPage[MyResourcesPage](context) {
  loadedBy = By.xpath("//h5[text()='My Resources']")

  override protected def loadUrl(): Unit = {
    driver.get(context.getBaseUrl + "page/myresources")
  }
}
