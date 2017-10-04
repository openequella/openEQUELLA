package equellatests.pages.moderate

import com.tle.webtests.framework.PageContext
import equellatests.pages.WaitingBrowserPage
import org.openqa.selenium.By

import scala.collection.JavaConverters._


class ModerationView(val ctx: PageContext) extends WaitingBrowserPage {
  def approve() : ApproveDialog = {
    findElement(By.className("moderate-approve")).click()
    new ApproveDialog(ctx).get()
  }

  def reject() : RejectDialog = {
    findElement(By.className("moderate-reject")).click()
    new RejectDialog(ctx).get()
  }

  override def pageBy = By.id("moderate")

  def postComment() : PostCommentDialog = {
    findElement(By.id("_tasks_postButton")).click()
    new PostCommentDialog(ctx).get()
  }

  def allComments() : Seq[ModerationComment] = {
    driver.findElements(By.xpath("id('moderation-comments')/div[contains(@class, 'modcomment')]")).asScala.map(ModerationComment(ctx))
  }

}
