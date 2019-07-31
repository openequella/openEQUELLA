package equellatests.domain

import org.scalacheck.Arbitrary
import Arbitrary._
import io.circe.{Decoder, Encoder}

case class ValidFilename(filename: String)

object ValidFilename {
  implicit val vfnEnc: Encoder[ValidFilename] = Encoder.encodeString.contramap(_.filename)
  implicit val vfnDec: Decoder[ValidFilename] = Decoder.decodeString.map(ValidFilename.apply)

  implicit val arbValidFilename = Arbitrary(for {
    urw <- arbitrary[UniqueRandomWord]
  } yield ValidFilename(urw.word))
}
