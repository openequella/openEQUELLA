package com.tle.admin.service

import com.tle.beans.user.TLEUser

import java.util.Optional

trait AdminTLEUserService {

  def add(user: TLEUser): String

  def get(uniqueId: String): Optional[TLEUser]

  def getByUsername(username: String): Optional[TLEUser]

  /**
    * Delete a user by UUID.
    *
    * @param uuid the UUID of the user to delete
    * @throws ClientRequestException if there are any errors deleting the user
    */
  def delete(uuid: String): Unit

  /**
    * Given an existing user's TLEUser entity which has been modified, update the user in the
    * database.
    *
    * @param user              The user to update
    * @param passwordNotHashed Whether the password is already hashed - if not, validate it meets
    *                          password requirements and hash it before updating the user.
    * @return The UUID of the updated user
    */
  def edit(user: TLEUser, passwordNotHashed: Boolean): String

  def searchUsers(query: String, parentGroupID: String, recursive: Boolean): java.util.List[TLEUser]
}
