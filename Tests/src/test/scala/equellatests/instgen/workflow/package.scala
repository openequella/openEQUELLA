package equellatests.instgen

import equellatests.GlobalConfig
import equellatests.domain.TestLogon

package object workflow {
  val workflowInst = GlobalConfig.createTestInst("workflow")
  val adminLogon = TestLogon("admin", "``````", workflowInst)

  def nextTask3Step(current: String) : Option[String] = {
    current match {
      case "Step 1" => Some("Step 2")
      case "Step 2" => Some("Step 3")
      case "Step 3" => None
    }
  }

  def rejectionTasks3Step(current: String): Seq[String] = current match {
    case "Step 1" => Seq()
    case "Step 2" => Seq("Step 1")
    case "Step 3" => Seq("Step 2", "Step 1")
  }
}
