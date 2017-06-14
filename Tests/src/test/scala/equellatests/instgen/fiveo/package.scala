package equellatests.instgen

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
