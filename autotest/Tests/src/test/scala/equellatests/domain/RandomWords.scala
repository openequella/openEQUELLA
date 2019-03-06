package equellatests.domain

import org.scalacheck.{Arbitrary, Gen}

object RandomWords {
  val words = {
    Seq("area", "snowdrift", "eggshell", "pewter", "raton", "nonirrigable", "loleta", "amnesty", "honeybunch", "sypher", "knave", "carracci", "reverifying", "ophorectomize", "submersible", "paralysed", "socotran", "orthographer", "professor", "plumbum", "macoma", "absentness", "ostracism", "tallyman", "gleam", "miscreative", "gesellschaft", "scope", "nonstarting", "tense", "recentralizing", "chekhovian", "blatantly", "preventoria", "purana", "nonsubmissive", "juru", "liang", "undarned", "prededicated", "bolognese", "velour", "hierodulic", "davout", "divinised", "immaculate", "bag", "skylarking", "crimping", "pilot", "dogfood", "solutions", "duck", "horse", "friend", "silly", "dump", "peaches", "lump", "plerp", "nerpa")

  }
  val someWords : Gen[RandomWords] = for {
    numWords <- Gen.choose(1, 5)
    words <- Gen.listOfN(numWords, Gen.oneOf(words))
  } yield RandomWords(words)

  implicit val arbWords : Arbitrary[RandomWords] = Arbitrary(someWords)

  def withNumberAfter(n: Int, v: RandomWords) : RandomWords = v.copy(v.words :+ s"($n)")

}

case class RandomWords(words:Seq[String]) {
  lazy val asString = words.mkString(" ")

}
