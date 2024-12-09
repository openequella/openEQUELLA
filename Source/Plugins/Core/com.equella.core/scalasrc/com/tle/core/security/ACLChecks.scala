/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0, (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
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

import com.tle.exceptions.PrivilegeRequiredException
import com.tle.legacy.LegacyGuice
import scala.jdk.CollectionConverters._

object ACLChecks {

  /** Checks if the current user has the specified ACL.
    *
    * @param privilege
    *   Required ACL, typically defined as a constant in `com.tle.common.security.SecurityConstants`
    * @param includePossibleOwnerAcls
    *   `true` to include possible owner ACLs
    */
  def hasAcl(privilege: String, includePossibleOwnerAcls: Boolean = false): Boolean = {
    LegacyGuice.aclManager.hasPrivilege(Set(privilege).asJava, includePossibleOwnerAcls)
  }

  /** Checks if the current user has the specified ACL or throws `PrivilegeRequiredException`.
    *
    * @param privilege
    *   Required ACL, typically defined as a constant in `com.tle.common.security.SecurityConstants`
    * @param includePossibleOwnerAcls
    *   `true` to include possible owner ACLs
    */
  def hasAclOrThrow(privilege: String, includePossibleOwnerAcls: Boolean = false): Unit = {
    if (!hasAcl(privilege, includePossibleOwnerAcls)) {
      throw new PrivilegeRequiredException(privilege)
    }
  }
}
