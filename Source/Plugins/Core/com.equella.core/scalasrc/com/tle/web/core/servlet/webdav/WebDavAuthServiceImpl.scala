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

import com.tle.core.guice.Bind
import com.tle.core.replicatedcache.ReplicatedCacheService
import com.tle.core.replicatedcache.ReplicatedCacheService.ReplicatedCache
import org.apache.commons.text.{CharacterPredicates, RandomStringGenerator}

import java.util.Base64
import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}

/** Storage format for the credentials stored in cache. The username and password are stored so that
  * if credentials for the same id are requested, the currently valid ones can be provided.
  *
  * @param username
  *   the username part.
  * @param password
  *   the password part.
  * @param encoded
  *   the HTTP Basic Auth encoded representation - for easy validation.
  * @param webUserDetails
  *   the details of the user from the oEQ Web UI side which is expected to use these credentials.
  */
@SerialVersionUID(1)
case class WebDavCredentials(
    username: String,
    password: String,
    encoded: String,
    webUserDetails: WebUserDetails
) extends Serializable
object WebDavCredentials {
  def apply(username: String, password: String, webUserDetails: WebUserDetails): WebDavCredentials =
    WebDavCredentials(
      username,
      password,
      Base64.getEncoder.encodeToString(s"$username:$password".getBytes),
      webUserDetails
    )
}

@Bind(classOf[WebDavAuthService])
@Singleton
class WebDavAuthServiceImpl(credStorage: ReplicatedCache[WebDavCredentials])
    extends WebDavAuthService {
  private val stringGenerator =
    (new RandomStringGenerator.Builder).filteredBy(CharacterPredicates.ASCII_ALPHA_NUMERALS).build()

  @Inject def this(rcs: ReplicatedCacheService) {
    // Not keen on the idea of having a limit on the number of cache entries, but that's the way
    // RCS works.
    this(
      rcs
        .getCache[WebDavCredentials]("webdav-creds", 1000, 1, TimeUnit.HOURS)
    )

  }

  override def createCredentials(
      id: String,
      oeqUserId: String,
      oeqUsername: String
  ): (String, String) =
    Option(credStorage.get(id).orNull())
      .map(c => (c.username, c.password))
      .getOrElse({
        // For username and password it is sufficient to have randomly generated alpha-numeric
        // strings between 10 and 20 characters.
        def generateString = stringGenerator.generate(10, 20)

        val username = generateString
        val password = generateString

        credStorage
          .put(id, WebDavCredentials(username, password, WebUserDetails(oeqUserId, oeqUsername)))

        (username, password)
      })

  override def removeCredentials(id: String): Unit = credStorage.invalidate(id)

  override def validateCredentials(
      id: String,
      authRequest: String
  ): Either[WebDavAuthError, Boolean] =
    Option(credStorage.get(id).orNull())
      .map { case WebDavCredentials(_, _, expectedPayload, _) =>
        if (expectedPayload == authRequest) Right(true)
        else Left(InvalidCredentials())
      }
      .getOrElse(Left(InvalidContext()))

  override def whois(id: String): Option[WebUserDetails] =
    Option(credStorage.get(id).orNull())
      .map(_.webUserDetails)
}
