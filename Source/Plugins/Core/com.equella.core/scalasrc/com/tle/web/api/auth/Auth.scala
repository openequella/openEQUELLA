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

package com.tle.web.api.auth

import com.tle.common.i18n.CurrentLocale
import com.tle.common.usermanagement.user.{CurrentUser, WebAuthenticationDetails}
import com.tle.exceptions.{
  AccountExpiredException,
  AuthenticationException,
  BadCredentialsException
}
import com.tle.legacy.LegacyGuice
import com.tle.web.resources.{PluginResourceHelper, ResourcesService}
import io.swagger.annotations.{Api, ApiOperation}
import javax.servlet.http.HttpServletRequest
import javax.ws.rs.core.{Context, Response}
import javax.ws.rs.{POST, PUT, Path, QueryParam}

@Api("Authentication")
@Path("auth")
class Auth {
  val RESOURCE_HELPER: PluginResourceHelper =
    ResourcesService.getResourceHelper(classOf[Auth])

  /** Provide simple username / password login as per a legacy oEQ form based authentication but for
    * use with REST APIs - possible the start of an authenticated Single Page App. This basically
    * mimics the existing form based login logic.
    *
    * @see
    *   com.tle.web.login.LogonSection#authenticate(SectionInfo)
    */
  @POST
  @Path("login")
  @ApiOperation(
    value = "Login as a normal user.",
    notes =
      "Provides a means to establish a simple cookie based (JSESSIONID) session, for easy use of the REST API for user based operations.",
    response = classOf[String]
  )
  def login(
      @Context req: HttpServletRequest,
      @QueryParam("username") username: String,
      @QueryParam("password") password: String
  ): Response = {
    LegacyGuice.userSessionService.reenableSessionUse()

    val us  = LegacyGuice.userService
    val wad = us.getWebAuthenticationDetails(req)
    def lfr(messageKey: String): Response =
      loginFailedResponse(wad, username, CurrentLocale.get(RESOURCE_HELPER.key(messageKey)))

    try {
      us.login(username, password, wad, true)
      Response.ok().build()
    } catch {
      case _: BadCredentialsException => lfr("logon.invalid")
      case e: AccountExpiredException => loginFailedResponse(wad, username, e.getMessage)
      case _: AuthenticationException => lfr("logon.problems")
    }
  }

  @PUT
  @Path("logout")
  @ApiOperation(
    value = "Logout the current session.",
    notes =
      "This is to logout sessions which were setup with the /api/auth/login endpoint, and will do so based on the JSESSIONID cookie."
  )
  def logout(@Context req: HttpServletRequest): Response = {
    LegacyGuice.userSessionService.reenableSessionUse()
    val us = LegacyGuice.userService
    us.logoutToGuest(us.getWebAuthenticationDetails(req), false)
    Response.ok().build()
  }

  def loginFailedResponse(
      wad: WebAuthenticationDetails,
      username: String,
      message: String
  ): Response = {
    LegacyGuice.auditLogService.logUserFailedAuthentication(username, wad)
    Response.status(401, message).entity(message).build()
  }
}
