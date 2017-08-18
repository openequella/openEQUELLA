package equellatests.instgen

import com.tle.webtests.framework.PageContext
import com.tle.webtests.pageobject.viewitem.SummaryPage
import com.tle.webtests.pageobject.wizard.controls.{EditBoxControl, UniversalControl}
import com.tle.webtests.pageobject.wizard.{ContributePage, WizardPageTab, WizardUrlPage}
import equellatests.GlobalConfig
import equellatests.domain.{TestInst, TestLogon}
import org.scalacheck.{Arbitrary, Gen}

package object fiveo {

  val fiveoInst : Gen[TestInst] = GlobalConfig.createTestInst("fiveo")

  implicit val logon = Arbitrary {
    for {
      i <- fiveoInst
    }
    yield TestLogon("AutoTest", "automated", i)
  }
}
