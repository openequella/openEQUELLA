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
import com.tle.common.institution.CurrentInstitution
import com.tle.common.util.UrlUtils.isAbsoluteHttpUrl
import com.tle.core.guice.Bind
import com.tle.core.services.user.UserService
import com.tle.integration.oauth2.error.{HasCode, OAuth2Error}
import com.tle.integration.oidc.service.OidcAuthService
import com.tle.integration.util.NO_FURTHER_INFO
import org.apache.http.client.utils.URIBuilder
import org.slf4j.LoggerFactory
import sttp.model.Uri

import java.net.URI
import javax.inject.{Inject, Named, Singleton}
import javax.servlet.http.{HttpServlet, HttpServletRequest, HttpServletResponse}
import scala.jdk.CollectionConverters._

/** This Servlet responds to GET requests sent to endpoint 'oidclogin.do' which is used as the
  * redirect URI of an OIDC integration.
  */
@Bind
@Singleton
class OidcCallbackServlet @Inject() (
    userService: UserService,
    authService: OidcAuthService
) extends HttpServlet {

  @Inject
  @Named("enable.oidc.token.logging")
  private var tokenLoggingEnabled: Boolean = _

  private val LOGGER = LoggerFactory.getLogger(classOf[OidcCallbackServlet])

  /** As per sections 3.1.2.7 and 3.1.3 of the OIDC spec, this endpoints is responsible for
    * performing the following tasks:
    *
    *   1. Attempt to retrieve an enabled Identity Provider configuration; 2. Verify the callback
    *      request; 3. Use the callback details and the configured Identity Provider to request an
    *      ID token; 4. Verify the ID token on receiving; 5. Attempt to log the user in with the
    *      verified token and the configuration; 6. Redirect user to the target page.
    *
    * Any step that fails will immediately stop the process and redirect user to the login page with
    * an error message.
    */
  override def doGet(req: HttpServletRequest, resp: HttpServletResponse): Unit = {
    val result = for {
      idp <- authService.getIdentityProvider

      callbackDetails <- authService.verifyCallbackRequest(req.getParameterMap.asScala.toMap)
      state        = callbackDetails.state
      stateDetails = callbackDetails.stateDetails
      code         = callbackDetails.code

      idToken <- authService.requestIdToken(code, stateDetails, idp)
      _ = if (tokenLoggingEnabled) LOGGER.debug(s"Retrieved ID Token: $idToken")
      verifiedToken <- authService.verifyIdToken(idToken, state, idp)
      _ <- authService.login(verifiedToken, userService.getWebAuthenticationDetails(req), idp)
    } yield stateDetails.targetPage

    val redirectTo = result match {
      case Right(targetPage) => redirectOnSuccess(targetPage)
      case Left(e)           => handleFailedAuthentication(e)
    }

    resp.sendRedirect(redirectTo.toString)
  }

  private def redirectOnSuccess(targetPage: Option[String]): URI = {
    val institution = Uri(CurrentInstitution.get().getUrlAsUri)
    val redirectTo  = targetPage match {
      case Some(p) if isAbsoluteHttpUrl(p) => p
      case Some(p)                         => s"$institution$p"
      case None                            => s"$institution${WebConstants.DASHBOARD_PAGE}"
    }

    Uri.parse(redirectTo) match {
      case Left(e) =>
        LOGGER.error(s"Failed to redirect to $redirectTo : $e")
        CurrentInstitution.get().getUrlAsUri
      case Right(uri) => uri.toJavaUri
    }
  }

  private def handleFailedAuthentication(e: OAuth2Error): URI = {
    val msg     = e.msg.getOrElse(NO_FURTHER_INFO)
    val fullMsg = e match {
      case err: HasCode[_] => s"${err.code.toString} - $msg"
      case _               => msg
    }

    val output = s"Single Sign-on failed: $fullMsg"
    LOGGER.error(output)

    // Include the NO_AUTO_LOGIN parameter so that errors that occur during the SSO process are displayed
    // in the Login page.
    Uri(CurrentInstitution.get().getUrlAsUri)
      .addPath(WebConstants.LOGIN_PAGE)
      .addParam(WebConstants.ERROR_PARAM, output)
      .addParam(WebConstants.NO_AUTO_LOGIN, "true")
      .toJavaUri
  }
}
