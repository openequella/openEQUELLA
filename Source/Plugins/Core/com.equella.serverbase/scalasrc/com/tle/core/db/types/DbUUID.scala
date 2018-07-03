package com.tle.core.db.types

import java.util.UUID

case class DbUUID(id: UUID) extends AnyVal

object DbUUID
{
  implicit def fromString(str: String): DbUUID = DbUUID(UUID.fromString(str))
}
