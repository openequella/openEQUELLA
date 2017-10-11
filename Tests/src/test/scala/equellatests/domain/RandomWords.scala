package equellatests.domain

import org.scalacheck.Gen

object RandomWords {
  val words = {
    Seq("area", "snowdrift", "eggshell", "pewter", "raton", "nonirrigable", "loleta", "amnesty", "honeybunch", "sypher", "knave", "carracci", "reverifying", "ophorectomize", "submersible", "paralysed", "socotran", "orthographer", "professor", "plumbum", "macoma", "absentness", "ostracism", "tallyman", "gleam", "miscreative", "gesellschaft", "scope", "nonstarting", "tense", "recentralizing", "chekhovian", "blatantly", "preventoria", "purana", "nonsubmissive", "juru", "liang", "undarned", "prededicated", "bolognese", "velour", "hierodulic", "davout", "divinised", "immaculate", "bag", "skylarking", "crimping", "pilot", "dogfood", "solutions", "duck", "horse", "friend", "silly", "dump", "peaches", "lump", "plerp", "nerpa")

  }
  val someWords = for {
    numWords <- Gen.choose(1, 5)
    words <- Gen.listOfN(numWords, Gen.oneOf(words))
  } yield RandomWords(words)
}

case class RandomWords(words:Seq[String]) {
  lazy val asString = words.mkString(" ")

}
