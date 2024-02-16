package com.tle.web.remoting.graphql.schema

case class User(uniqueId: String,
                username: String,
                email: String,
                firstName: String,
                lastName: String)
object User {
  def apply(u: com.tle.beans.user.TLEUser): User =
    User(u.getUniqueID, u.getUsername, u.getEmailAddress, u.getFirstName, u.getLastName)

}
