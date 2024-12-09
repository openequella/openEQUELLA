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

package com.tle.web.core.servlet.webdav

/** Details of the user from the oEQ Web UI side.
  *
  * @param uniqueId
  *   the unique id of the user - typically a UUID
  * @param username
  *   the username of the user - typically human readable
  */
@SerialVersionUID(1)
case class WebUserDetails(uniqueId: String, username: String) extends Serializable

sealed abstract class WebDavAuthError(msg: String) {
  override def toString: String = msg
}

case class InvalidCredentials() extends WebDavAuthError("Invalid username and/or password")

case class InvalidContext() extends WebDavAuthError("No matching context ID.")

/** A Service used to authenticate credentials of WebDAV sessions targeted to a specific context.
  * The logic encapsulated here is to support the requirements of the WebDavServlet used for
  * uploading files as attachments to items. With the context then being a reference to the WebDAV
  * location - and as a result, a degree of authorisation is also being provided.
  */
trait WebDavAuthService {

  /** Given an `id` representing a context, return a unique (to that context) set of credentials
    * which can later be used to authenticate against that context. Some implementations are
    * expected to store these credentials in a TTL based store (cache) to provide automatic expiry.
    * However that is optional and left to the implementation.
    *
    * @param id
    *   a unique value representing a context for which these credentials will then be valid.
    * @param oeqUserId
    *   the unique id of the user who will be authenticating with these credentials - typically a
    *   UUID
    * @param oeqUsername
    *   the username of the user who will be authenticating with these credentials - typically human
    *   readable
    * @return
    *   a tuple containing a 'username' and 'password'.
    */
  def createCredentials(id: String, oeqUserId: String, oeqUsername: String): (String, String)

  /** Destroys the credentials for the provided context.
    *
    * @param id
    *   the id of the context for which credentials should be cleared.
    */
  def removeCredentials(id: String): Unit

  /** Receives the value of a HTTP Basic Authorization header (`authRequest`) and validates this
    * against the credentials stored for the specified context (`id`).
    *
    * @param id
    *   the context for which the provided credentials are expected to be valid.
    * @param authRequest
    *   the payload of the HTTP Basic Authorization header as described at
    *   <https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Authorization>
    * @return
    *   either a `Left[WebDavAuthError]` with an error message describing what failed, otherwise
    *   `Right(true)` indicate valid credentials.
    */
  def validateCredentials(id: String, authRequest: String): Either[WebDavAuthError, Boolean]

  /** Given an `id` representing a context, return the details of the user who created the
    * credentials for that context.
    *
    * @param id
    *   the identifier used for the context
    * @return
    *   if there are credentials registered for the specified `id` then the details of the user who
    *   created them, otherwise `None`.
    */
  def whois(id: String): Option[WebUserDetails]
}
