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

package com.tle.core.usermanagement

import cats.implicits._
import com.tle.beans.ump.UserManagementSettings
import com.tle.common.usermanagement.user.{DefaultUserState, ModifiableUserState}
import com.tle.integration.oidc.idp.{IdentityProviderDetails, IdentityProviderPlatform}
import com.tle.integration.oidc.service.OidcConfigurationService
import com.tle.plugins.ump.AbstractUserDirectory
import javax.inject.Inject

/** Abstract User Directory to be used when the OIDC integration is enabled.
  */
abstract class OidcUserDirectory extends AbstractUserDirectory {

  @Inject protected var oidcConfigurationService: OidcConfigurationService = _

  // Not required in OIDC User Directories so return false.
  override protected def initialise(settings: UserManagementSettings): Boolean = false

  /** Type alias for the Identity Provider details which must be a concrete subtype of
    * [[IdentityProviderDetails]].
    */
  protected type IDP <: IdentityProviderDetails

  /** Type alias for the result of an authentication request (e.g. Access token for OAuth2
    */
  protected type AuthResult

  /** The platform which a UserDirectory implementation works for.
    */
  protected val targetPlatform: IdentityProviderPlatform.Value

  /** Perform an authentication with the configured Identity Provider and return the result.
    */
  protected def authenticate(idp: IDP): Either[Throwable, AuthResult]

  /** Execute the provided user search request through three steps:
    *
    *   1. Retrieve an enabled Identity Provider configured for the target platform; 2. Request an
    *      OAuth2 access token from the Identity Provider; 3. Execute the request and return the
    *      result.
    *
    * @param request
    *   Function that requests resources from the Identity Provider with the configured IdP and the
    *   authentication result.
    */
  protected def execute[T](
      request: (IDP, AuthResult) => Either[Throwable, T]
  ): Either[Throwable, T] = {
    for {
      idp <- oidcConfigurationService.get
        .filterOrElse(
          _.commonDetails.enabled,
          new IllegalStateException(s"The OIDC configuration is disabled.")
        )
        .filterOrElse(
          _.commonDetails.platform == targetPlatform,
          new IllegalStateException(
            s"The OIDC configuration doesn't work for platform $targetPlatform."
          )
        )
        .flatMap(idp => Either.catchNonFatal(idp.asInstanceOf[IDP]))
      authResult <- authenticate(idp)
      result     <- request(idp, authResult)
    } yield result
  }

  override def authenticateUserFromUsername(
      username: String,
      privateData: String
  ): ModifiableUserState =
    Option(getInformationForUser(username)).map { user =>
      val userState = new DefaultUserState
      userState.setLoggedInUser(user)
      userState
    }.orNull
}
