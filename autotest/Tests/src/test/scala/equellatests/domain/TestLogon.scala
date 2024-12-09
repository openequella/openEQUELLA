package equellatests.domain

import io.circe.generic.semiauto._

case class TestLogon(
    username: String,
    password: String,
    inst: TestInst,
    firstName: String = "",
    lastName: String = ""
) {
  def fullName = s"$firstName $lastName"
}

object TestLogon {

  implicit val testLogonEnc = deriveEncoder[TestLogon]
  implicit val testLogonDec = deriveDecoder[TestLogon]

}
