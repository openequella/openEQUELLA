package equellatests.instgen

import equellatests.GlobalConfig
import equellatests.domain.TestLogon

package object workflow {
  val workflowInst = GlobalConfig.createTestInst("workflow")
  val adminLogon = TestLogon("admin", "``````", workflowInst)
}
