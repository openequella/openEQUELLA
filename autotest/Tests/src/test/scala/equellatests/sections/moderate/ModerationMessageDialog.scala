package equellatests.sections.moderate

import com.tle.webtests.framework.{PageContext, ScreenshotTaker}
import com.tle.webtests.pageobject.generic.component.EquellaSelect
import equellatests.browserpage.{BrowserPage, WaitingBrowserPage}
import equellatests.domain.TestFile
import equellatests.pages.moderate.TaskListPage
import org.openqa.selenium.{By, WebElement}
import org.openqa.selenium.support.ui.{ExpectedCondition, ExpectedConditions}
import scala.jdk.CollectionConverters._

abstract class ModerationMessageDialog(pfx: String) extends WaitingBrowserPage {

  def uploadFileError(tf: TestFile, filename: String): Unit = {
    val finished = updatedBy(By.xpath("id('uploads')/*[1]"))
    uploadAndWait(tf, filename, finished)
  }

  private def uploadAndWait(tf: TestFile, filename: String, finished: ExpectedCondition[_]) = {
    byId("_fd_file").sendKeys(TestFile.realFile(tf, filename).getAbsolutePath)
    waitFor(finished)
  }

  def errorMessage: String = findElementById("comment-error").getText

  def submitError(): Unit = {
    val expect = updatedExpectation()
    byId("_ok").click()
    waitFor(expect)
  }

  def byId(post: String)   = findElementById(pfx + post)
  def msgBy                = By.id(pfx + "_commentField")
  def msgField: WebElement = pageElement
  def message: String      = msgField.getText
  def message_=(msg: String) = {
    msgField.clear()
    msgField.sendKeys(msg)
  }
  override def pageBy = msgBy

  def submit(): ModerationView = {
    val mv     = new ModerationView(ctx)
    val expect = mv.updatedExpectation()
    byId("_ok").click()
    waitFor(expect)
    mv
  }

  def submitModeration(): TaskListPage = {
    byId("_ok").click()
    new TaskListPage(ctx).get()
  }

  def cancel(): ModerationView = {
    val mv     = new ModerationView(ctx)
    val expect = ExpectedConditions.stalenessOf(byId("_f"))
    byId("_c").click()
    waitFor(expect)
    mv.get
  }

  def uploadFile(tf: TestFile, filename: String): Unit = {
    val finished = ExpectedConditions.visibilityOfElementLocated(
      By.xpath(
        s"//div[@class='file-upload' " +
          s"and //a[text()=${quoteXPath(filename.toLowerCase)}] " +
          s"and //div[@class='progress-bar' and div[contains(@class, 'complete')]]]"
      )
    )
    uploadAndWait(tf, filename, finished)
  }
}

class PostCommentDialog(val ctx: PageContext) extends ModerationMessageDialog("_taskscommentDialog")

class ApproveDialog(val ctx: PageContext) extends ModerationMessageDialog("_tasksapproveDialog")

class RejectDialog(val ctx: PageContext) extends ModerationMessageDialog("_tasksrejectDialog") {
  def rejectElem                       = new EquellaSelect(ctx, byId("_rejectSteps"))
  def rejectStep: String               = rejectElem.getSelectedText
  def rejectStep_=(step: String): Unit = rejectElem.selectByVisibleText(step)
}

case class ModerationComment(ctx: PageContext)(elem: WebElement) extends BrowserPage {
  def commentClass: String = {
    val clazzes = elem.getAttribute("class")
    if (clazzes.startsWith("modcomment ")) clazzes.substring("modcomment ".size) else ""
  }

  def fileNames: Set[String] =
    elem.findElements(By.xpath(".//ul/li/a")).asScala.map(_.getText).toSet

  def message: String =
    elem
      .findElement(
        By.xpath("div[@class = 'modcomment-content']/div[@class = 'modcomment-content']")
      )
      .getText
}
