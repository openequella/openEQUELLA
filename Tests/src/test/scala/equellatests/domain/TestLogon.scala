package equellatests.domain

import io.circe.generic.semiauto._

case class TestLogon(username: String, password: String, inst: TestInst)

object TestLogon {

  implicit val testLogonEnc = deriveEncoder[TestLogon]
  implicit val testLogonDec = deriveDecoder[TestLogon]

}
