package equellatests.pages.moderate

import com.tle.webtests.framework.PageContext
import equellatests.domain.{TestFile, ValidFilename}
import equellatests.pages.{BrowserPage, WaitingBrowserPage}
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.{By, WebElement}

import scala.collection.JavaConverters._

abstract class ModerationMessageDialog(pfx: String) extends WaitingBrowserPage {
  def byId(post: String) = findElementById(pfx+post)
  def msgBy = By.id(pfx+"_commentField")
  def msgField : WebElement = pageElement
  def message : String = msgField.getText
  def message_=(msg : String) = {
    msgField.clear()
    msgField.sendKeys(msg)
  }
  override def pageBy = msgBy

  def submit() : ModerationView = {
    val mv = new ModerationView(ctx)
    val expect = mv.updatedExpectation()
    byId("_ok").click()
    waitFor(expect)
    mv
  }

  def uploadFile(tf: TestFile, filename: String) : Unit = {
    val finished = ExpectedConditions.visibilityOfElementLocated(By.xpath(s"//div[@class='file-upload' " +
      s"and //a[text()=${quoteXPath(filename.toLowerCase)}] " +
      s"and //div[@class='progress-bar' and div[contains(@class, 'complete')]]]"))
    byId("_fd_file").sendKeys(TestFile.realFile(tf, filename).getAbsolutePath)
    waitFor(finished)
  }
}

class PostCommentDialog(val ctx: PageContext) extends ModerationMessageDialog("_taskscommentDialog")

case class ModerationComment(ctx: PageContext)(elem: WebElement) extends BrowserPage
{
  def fileNames: Set[String] = elem.findElements(By.xpath(".//ul/li/a")).asScala.map(_.getText).toSet

  def message: String = elem.findElement(By.xpath("div[@class = 'modcomment-content']/div[@class = 'modcomment-content']")).getText
}

class ModerationView(val ctx: PageContext) extends WaitingBrowserPage {
  override def pageBy = By.id("moderate")

  def postComment() : PostCommentDialog = {
    findElement(By.id("_tasks_postButton")).click()
    new PostCommentDialog(ctx).get()
  }

  def allComments() : Seq[ModerationComment] = {
    driver.findElements(By.xpath("id('moderation-comments')/div[contains(@class, 'modcomment')]")).asScala.map(ModerationComment(ctx))
  }

}
