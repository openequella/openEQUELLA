package equellatests.instgen

import java.net.URI

import com.tle.webtests.framework.TestConfig
import equellatests.GlobalConfig
import equellatests.domain.{TestInst, TestLogon}
import org.scalacheck.{Arbitrary, Gen}
import org.scalacheck.Arbitrary._

package object fiveo {

  val config = TestConfig.getConfigProps

  val fiveoInst : Gen[TestInst] = GlobalConfig.createTestInst("fiveo")

  implicit val logon = Arbitrary {
    for {
      i <- fiveoInst
    }
    yield TestLogon("AutoTest", "automated", i)
  }
}
