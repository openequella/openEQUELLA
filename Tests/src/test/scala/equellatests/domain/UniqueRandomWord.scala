package equellatests.domain

import java.util.UUID

import org.scalacheck.{Arbitrary, Gen}

// A random ascii word that is guaranteed to be unique
case class UniqueRandomWord(word: String)


object UniqueRandomWord {
  implicit val genURW : Gen[UniqueRandomWord] = Gen.size.map(_ => UniqueRandomWord(UUID.randomUUID().toString.filterNot(_ == '-')))
  implicit val arbURW : Arbitrary[UniqueRandomWord] = Arbitrary(genURW)
}

