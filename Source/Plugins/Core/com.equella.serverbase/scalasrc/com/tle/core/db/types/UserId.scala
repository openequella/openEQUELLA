package com.tle.core.db.types

import io.doolse.simpledba.jdbc.SizedIso

case class UserId(id: String) extends AnyVal

object UserId
{
  implicit def userIdIso: SizedIso[UserId, String] = SizedIso(255, _.id, UserId.apply)
}