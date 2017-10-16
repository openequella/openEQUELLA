package equellatests.pages.search

import equellatests.pages.moderate.ModerationView
import equellatests.pages.{CommonXPath, WaitingBrowserPage}

trait TaskResult extends WaitingBrowserPage {

  def moderate(): ModerationView = {
    pageElement.findElement(CommonXPath.buttonWithText("Moderate")).click()
    new ModerationView(ctx).get()
  }
}
