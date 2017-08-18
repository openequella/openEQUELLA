package equellatests.domain

import org.scalacheck.{Arbitrary, Gen}

case class ValidDescription(desc: String)

object ValidDescription {
  implicit val arbValidDesc = Arbitrary {
    Gen.alphaNumStr.map(s => ValidDescription(if (s.isEmpty) "_" else s))
  }
}