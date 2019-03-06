package equellatests.domain

import org.scalacheck.{Arbitrary, Gen}

sealed trait BooleanOp

case object And extends BooleanOp
case object Or extends BooleanOp

object BooleanOp {
  implicit val arbOp : Arbitrary[BooleanOp] = Arbitrary(Gen.oneOf(And, Or))
}