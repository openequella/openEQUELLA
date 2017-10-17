package equellatests.pages.search

import equellatests.pages.moderate.ModerationView
import equellatests.pages.{CommonXPath, WaitingBrowserPage}

trait TaskResult extends MetadataResult {

  def moderate(): ModerationView = {
    pageElement.findElement(CommonXPath.buttonWithText("Moderate")).click()
    new ModerationView(ctx).get()
  }

  def taskOn: String = metadataText("Task:")

}
