/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
