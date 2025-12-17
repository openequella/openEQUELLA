package io.github.openequella.pages.myresources

import com.tle.webtests.framework.PageContext
import io.github.openequella.pages.search.AbstractSearchPage
import org.openqa.selenium.By

class NewMyResourcesPage(context: PageContext)
    extends AbstractSearchPage[NewMyResourcesPage](context) {
  loadedBy = By.xpath("//h5[text()='My Resources']")
}
