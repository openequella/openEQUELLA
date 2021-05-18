package com.tle.core.security

import com.tle.exceptions.PrivilegeRequiredException
import com.tle.legacy.LegacyGuice
import scala.collection.JavaConverters._

object ACLChecks {

  /**
    * Checks if the current user has the specified ACL.
    *
    * @param privilege Required ACL, typically defined as a constant in `com.tle.common.security.SecurityConstants`
    * @param includePossibleOwnerAcls `true` to include possible owner ACLs
    */
  def hasAcl(privilege: String, includePossibleOwnerAcls: Boolean = true): Boolean = {
    LegacyGuice.aclManager.hasPrivilege(Set(privilege).asJava, includePossibleOwnerAcls)
  }

  /**
    * Checks if the current user has the specified ACL or throws `PrivilegeRequiredException`.
    *
    * @param privilege Required ACL, typically defined as a constant in `com.tle.common.security.SecurityConstants`
    * @param includePossibleOwnerAcls `true` to include possible owner ACLs
    */
  def hasAclOrThrow(privilege: String, includePossibleOwnerAcls: Boolean = false): Unit = {
    if (!hasAcl(privilege, includePossibleOwnerAcls)) {
      throw new PrivilegeRequiredException(privilege)
    }
  }
}
