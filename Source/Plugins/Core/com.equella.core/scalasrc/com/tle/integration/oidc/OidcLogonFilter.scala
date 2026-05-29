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

package com.tle.integration.oidc

import com.dytech.edge.web.WebConstants
import com.tle.common.usermanagement.user.UserState
import com.tle.core.guice.Bind
import com.tle.integration.oidc.idp.CommonDetails
import com.tle.integration.oidc.service.{OidcAuthService, OidcConfigurationService}
import com.tle.plugins.ump.UserManagementLogonFilter
import com.tle.web.dispatcher.FilterResult
import sttp.model.Uri

import java.net.URI
import java.util
import javax.inject.{Inject, Singleton}
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

/** Provides support for OIDC Seamless SSO by intercepting logon and logout requests that directly
  * hit the `/logon.do` endpoint.
  *
  * Behavior when Seamless SSO is enabled:
  *   - Logon requests are automatically redirected to the configured Identity Provider (IdP).
  *   - If the user has an active IdP session, they are seamlessly redirected back to the target OEQ
  *     resource.
  *   - If no active session exists, the user will remain on the IdP login page to authenticate.
  *   - Logout requests are processed as normal, but the logout URI is modified to include a
  *     parameter that prevents automatic redirection back to the IdP, allowing users to land on the
  *     OEQ native login page.
  */
@Singleton
@Bind
class OidcLogonFilter extends UserManagementLogonFilter {

  @Inject
  private var authService: OidcAuthService = _

  @Inject
  private var oidcConfigurationService: OidcConfigurationService = _

  override def init(attributes: util.Map[AnyRef, AnyRef]): Boolean = true

  /** Process logon and logout request. If the request is for logout, then continue with the normal
    * flow. If the request is for logon, then check if Seamless SSO is enabled and if so, redirect
    * to the IdP for authentication.
    */
  override def filter(request: HttpServletRequest, response: HttpServletResponse): FilterResult = {
    val isLogout =
      Option(request.getParameterMap.get(WebConstants.LOGOUT))
        .flatMap(_.headOption)
        .flatMap(_.toBooleanOption)
        .getOrElse(false)

    // Continue the normal login process when:
    //  - The request is for logout (indicated by the presence of the 'logout' parameter); or
    //  - The request includes the parameter indicating auto-login should be bypassed.
    // In all other cases, attempt to perform an auto-login using Seamless SSO.
    if (isLogout || authService.shouldBypassAutoLogin(request.getParameterMap)) {
      FilterResult.FILTER_CONTINUE
    } else {
      autoLogin(request, response)
    }
  }

  /** When Seamless SSO is enabled, this appends the 'NO_AUTO_LOGIN' parameter to the standard OEQ
    * logout URI. This ensures that users land on the native OEQ login page upon logout, preventing
    * the IdP from automatically logging them right back in.
    *
    * @param state
    *   State of the current user.
    * @param loggedOutURI
    *   The standard OEQ logout URI.
    */
  override def logoutURI(state: UserState, loggedOutURI: URI): URI = {
    oidcConfigurationService.getForSeamlessSso match {
      case Some(_) =>
        Uri(loggedOutURI).addParam(WebConstants.NO_AUTO_LOGIN, "true").toJavaUri
      case None => loggedOutURI
    }
  }

  override def logoutRedirect(loggedOutURI: URI): URI = loggedOutURI

  override def addStateParameters(
      request: HttpServletRequest,
      params: util.Map[String, Array[String]]
  ): Unit = {
    // Note: Adding the 'NO_AUTO_LOGIN' parameter to the supplied Map is required to
    // support bypassing the auto-login in Old UI. The result of doing this is there
    // will be a hidden `input`  under form 'eqpageForm' for 'NO_AUTO_LOGIN'. Then,
    // on login submission, the value of this parameter will be included in the request
    // payload, which allows the filter to access this parameter.
    Option(request.getParameter(WebConstants.NO_AUTO_LOGIN)) match {
      case Some(noAutoLogin) => params.put(WebConstants.NO_AUTO_LOGIN, Array(noAutoLogin))
      case None              => // Do nothing if the parameter is not present in the request
    }
  }

  private def performSeamlessSsoRedirect(
      request: HttpServletRequest,
      response: HttpServletResponse,
      details: CommonDetails
  ): FilterResult = {
    val target  = Option(request.getParameter(WebConstants.PAGE_PARAM)).orNull
    val authUrl = authService.buildAuthUrl(
      details.authUrl.toString,
      details.authCodeClientId,
      target
    )
    response.sendRedirect(authUrl)
    // Return a FilterResult with `stop` being `true` to indicate that the filter chain should stop.
    new FilterResult(true)
  }

  // If there is an OIDC configuration for Seamless SSO, redirect the request to IdP for auto-login.
  // Otherwise, continue the normal login process.
  private def autoLogin(
      request: HttpServletRequest,
      response: HttpServletResponse
  ): FilterResult = {
    oidcConfigurationService.getForSeamlessSso match {
      case Some(details) => performSeamlessSsoRedirect(request, response, details)
      case None          => FilterResult.FILTER_CONTINUE
    }
  }
}
