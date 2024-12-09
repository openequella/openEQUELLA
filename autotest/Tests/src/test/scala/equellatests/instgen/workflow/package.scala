package equellatests.instgen

import java.util.UUID

import equellatests.GlobalConfig
import equellatests.domain.TestLogon

package object workflow {
  val workflowInst = GlobalConfig.createTestInst("workflow")
  val adminLogon   = TestLogon("admin", "``````", workflowInst, "ad", "min")
  val tleAdminLogon = TestLogon(
    "TLE_ADMINISTRATOR",
    GlobalConfig.testConfig.getAdminPassword,
    workflowInst,
    "ad",
    "min"
  )

  val workflow3StepTasks                = Seq("Step 1", "Step 2", "Step 3")
  def workflow3StepBefore(task: String) = workflow3StepTasks.take(workflow3StepTasks.indexOf(task))

  def simpleMetadata(name: String) = s"<xml><name>$name</name></xml>"

  def nextTask3Step(current: String): Option[String] = {
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

  def nameToUsername(name: String): String = name match {
    case "Simple Moderator" => "SimpleModerator"
    case "ad min"           => "admin"
  }

  def usernameToId(username: String): String = username match {
    case "admin"           => "83bb1131-e54c-6f1e-e063-9d00597c8d97"
    case "SimpleModerator" => "d58b8087-7d64-2115-c187-20e5eb890743"
  }

  val threeStepWMUuid = UUID.fromString("51925f8b-86f9-4078-844f-3f1b3089f16c")

}
