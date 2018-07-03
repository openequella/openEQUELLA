package com.tle.core.db.types

import io.circe.{Decoder, Encoder}
import io.doolse.simpledba.Iso
import io.circe.syntax._
import io.circe.parser._

trait JsonColumn

object JsonColumn
{
  def mkCirceIso[A : Encoder : Decoder](default: A): Iso[A, Option[String]] = Iso(a => Some(a.asJson.noSpaces), _.map {
    s => decode[A](s).fold(throw _, identity)
  }.getOrElse(default))
}