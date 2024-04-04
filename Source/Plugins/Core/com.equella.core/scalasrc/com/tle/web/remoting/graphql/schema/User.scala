package com.tle.web.remoting.graphql.schema

import com.tle.beans.user.TLEUser

/**
  * A universal representation of users, for which the various user types in oEQ will be mapped to.
  */
case class User(uniqueId: String,
                username: String,
                email: String,
                firstName: String,
                lastName: String)

/**
  * Companion object for `User` to provide a conversion from the various oEQ user types.
  */
object User {
  def apply(u: TLEUser): User =
    User(u.getUniqueID, u.getUsername, u.getEmailAddress, u.getFirstName, u.getLastName)

}
