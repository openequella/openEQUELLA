package equellatests.domain

import org.scalacheck.{Arbitrary, Gen}
import org.scalacheck.Arbitrary._

case class ValidDescription(desc: String)

object ValidDescription {
  implicit val arbValidDesc = Arbitrary {
    for {
      words <- arbitrary[RandomWords]
    } yield ValidDescription(words.asString)
  }

  def addNumber(num: Int, d: ValidDescription) = ValidDescription(s"${d.desc}_${num}")
}
