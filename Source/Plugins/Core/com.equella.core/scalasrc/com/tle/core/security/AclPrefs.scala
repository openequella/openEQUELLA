package com.tle.core.security

import com.tle.beans.security.AccessExpression
import com.tle.core.settings.UserPrefs

sealed trait ExpressionError
case class InvalidTarget(msg: String) extends ExpressionError

object AclPrefs {

  private val RECENT_TARGETS = "acl.recenttargets"
  private val MAX_RECENT = 10

  def getRecentTargets : Iterable[String] =
    UserPrefs.jsonPref[Iterable[String]](RECENT_TARGETS).getOrElse(Iterable.empty)


  def addRecent(target: String): Option[ExpressionError] = {
    if (target.contains(" ")) Some(InvalidTarget("Contains space")) else {
      UserPrefs.setJsonPref(RECENT_TARGETS, (target :: getRecentTargets.toList).distinct.take(MAX_RECENT))
      None
    }

  }
}
