package com.tle.core.db.types

import io.doolse.simpledba.jdbc.SizedIso

case class DbUUID(id: String) extends AnyVal

object DbUUID
{
  implicit val sizedIso: SizedIso[DbUUID, String] = SizedIso(40, _.id, DbUUID.apply)

  implicit def fromString(str: String): DbUUID = DbUUID(str)
}
