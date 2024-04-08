package com.tle.web.remoting.graphql.schema

import caliban.schema.Annotations.GQLDescription
import com.tle.beans.user.TLEUser

/**
  * A universal representation of users, for which the various user types in oEQ will be mapped to.
  */
case class User(@GQLDescription("The unique identifier for the user") uniqueId: String,
                @GQLDescription("The username a user authenticates with") username: String,
                @GQLDescription("User's email address") email: Option[String],
                @GQLDescription("User's first name") firstName: String,
                @GQLDescription("User's last name") lastName: String)

/**
  * Companion object for `User` to provide a conversion from the various oEQ user types.
  */
object User {
  def apply(u: TLEUser): User =
    User(u.getUniqueID, u.getUsername, Option(u.getEmailAddress), u.getFirstName, u.getLastName)

}
