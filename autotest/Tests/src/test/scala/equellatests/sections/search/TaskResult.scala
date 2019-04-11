package equellatests.sections.search

import equellatests.browserpage.CommonXPath
import equellatests.sections.moderate.ModerationView

trait TaskResult extends MetadataResult {

  def moderate(): ModerationView = {
    pageElement.findElement(CommonXPath.buttonWithText("Moderate")).click()
    new ModerationView(ctx).get()
  }

  def taskOn: String = metadataText("Task:")

  def assignedTo(me: String): Option[String] = metadataText("Assigned to:") match {
    case "Me"         => Some(me)
    case "Unassigned" => None
    case o            => Some(o)
  }
}
