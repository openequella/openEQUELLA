package equellatests.domain

import org.scalacheck.{Arbitrary, Gen}

case class RandomWord(word: String, cased: String)

object RandomWord {
  implicit val arbWord = Arbitrary(for {
    sz    <- Gen.choose(5, 10)
    chars <- Gen.listOfN(sz, Gen.alphaChar)
    ct    <- Gen.choose(0, 2)
  } yield {
    val nc = new String(chars.toArray)
    val cased = ct match {
      case 0 => nc
      case 1 => nc.toLowerCase
      case _ => nc.toUpperCase
    }
    RandomWord(nc, cased)
  })
}
