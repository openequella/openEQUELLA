package equellatests.domain

import org.scalacheck.Arbitrary
import Arbitrary._

case class ValidFilename(filename: String)

object ValidFilename {
  implicit val arbValidFilename = Arbitrary(for {
    urw <- arbitrary[UniqueRandomWord]
  } yield ValidFilename(urw.word)
  )
}
