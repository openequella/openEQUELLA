package com.tle.core.settings

import com.tle.legacy.LegacyGuice
import io.circe.{Decoder, Encoder}
import io.circe.parser._
import io.circe.syntax._

object UserPrefs {

  def jsonPref[A](key: String)(implicit d: Decoder[A]): Option[A] = {
    Option(LegacyGuice.userPreferenceService.getPreference(key)).flatMap { p =>
      parse(p).flatMap(d.decodeJson).toOption
    }
  }

  def setJsonPref[A : Encoder](k: String, a: A): Unit = {
    LegacyGuice.userPreferenceService.setPreference(k, a.asJson.spaces2)
  }
}
