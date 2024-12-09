package equellatests.sections.search

import com.tle.webtests.framework.PageContext
import equellatests.browserpage.WaitingBrowserPage
import org.openqa.selenium.By

case class BulkAssignUserPage(ctx: PageContext) extends WaitingBrowserPage {

  def search(userQuery: String): Unit = {
    val expect    = updatedExpectation()
    val queryElem = findElementById("bwrto_q")
    queryElem.clear()
    queryElem.sendKeys(userQuery)
    findElementById("bwrto_s").click()
    waitFor(expect)
  }

  def selectByUsername(username: String): Unit = {
    val expect = updatedExpectation()
    val radio = pageElement.findElement(
      By.xpath(s".//ul/li[div[@class='username' and text() = ${quoteXPath(username)}]]/input")
    )
    radio.click()
    waitFor(expect)
  }

  override def pageBy = By.xpath("//div[@class = 'selectuser' and h3[text()='Select assignee']]")
}
