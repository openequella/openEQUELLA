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

package com.tle.integration.lti13

import com.tle.common.usermanagement.user.WebAuthenticationDetails
import com.tle.core.guice.Bind
import com.tle.core.services.user.UserService
import com.tle.integration.lti13.Lti13Request.getLtiRequestDetails
import com.tle.integration.oauth2.error.authorisation.AuthErrorResponse
import org.apache.http.HttpStatus
import org.slf4j.LoggerFactory
import javax.inject.{Inject, Singleton}
import javax.servlet.http.{HttpServlet, HttpServletRequest, HttpServletResponse}
import scala.jdk.CollectionConverters._

/** Handles the OpenID Connect Launch Flow as outlined in section 5.1 of the LTI 1.3 spec.
  */
@Bind
@Singleton
class OpenIDConnectLaunchServlet extends HttpServlet {
  private val LOGGER = LoggerFactory.getLogger(classOf[OpenIDConnectLaunchServlet])

  @Inject private var lti13AuthService: Lti13AuthService               = _
  @Inject private var stateService: Lti13StateService                  = _
  @Inject private var userService: UserService                         = _
  @Inject private var lti13IntegrationService: Lti13IntegrationService = _
  @Inject private var lti13PlatformService: Lti13PlatformService       = _
  @Inject private var lti13tokenValidator: Lti13TokenValidator         = _

  override def doGet(req: HttpServletRequest, resp: HttpServletResponse): Unit = {
    LOGGER.debug("doGet() called")

    // the initial launch should also be supported by GET - the other requests
    // (e.g. AuthenticationResponse) only require POST
    InitiateLoginRequest(req.getParameterMap.asScala.toMap) match {
      case Some(initReq) => handleInitiateLoginRequest(initReq, resp)
      case None =>
        resp.sendError(
          HttpServletResponse.SC_BAD_REQUEST,
          "Unsupported 'GET' LTI 1.3 launch request received."
        )
    }

    LOGGER.debug("doGet() complete")
  }

  override def doPost(req: HttpServletRequest, resp: HttpServletResponse): Unit = {
    LOGGER.debug("doPost() called")

    // TODO Needs validation around the request - was it a form POST request, does it have any params, etc.

    val params = req.getParameterMap.asScala.toMap
    val processedRequest =
      InitiateLoginRequest(params) orElse AuthenticationResponse(params) orElse AuthErrorResponse(
        params
      )

    processedRequest match {
      case Some(validRequest) =>
        validRequest match {
          case initReq: InitiateLoginRequest => handleInitiateLoginRequest(initReq, resp)
          case authResp: AuthenticationResponse =>
            handleAuthenticationResponse(
              authResp,
              userService.getWebAuthenticationDetails(req),
              req,
              resp
            )
          case errorResponse: AuthErrorResponse => handleErrorResponse(errorResponse, resp)
        }
      case None =>
        resp.sendError(
          HttpServletResponse.SC_BAD_REQUEST,
          "Unsupported LTI 1.3 launch request received."
        )
    }

    LOGGER.debug("doPost() complete")
  }

  private def handleInitiateLoginRequest(
      initLogin: InitiateLoginRequest,
      resp: HttpServletResponse
  ): Unit = {
    LOGGER.debug("Received a request to initiate a login. Supplied values:")
    LOGGER.debug(initLogin.toString)
    lti13AuthService.buildAuthReqUrl(initLogin).map(resp.encodeRedirectURL) match {
      case Some(authRedirectUrl) => resp.sendRedirect(authRedirectUrl)
      case None =>
        resp.sendError(
          HttpServletResponse.SC_BAD_REQUEST,
          "Unable to start launch with provided request."
        )
    }
  }

  private def handleAuthenticationResponse(
      auth: AuthenticationResponse,
      wad: WebAuthenticationDetails,
      req: HttpServletRequest,
      resp: HttpServletResponse
  ): Unit = {
    LOGGER.debug("Received an authentication response. Supplied values:")
    LOGGER.debug(auth.toString)

    // If this was not just for LTI Launch then we'd actually need to also support returning
    // the error details to a redirect URL. As the 1EdTech Security framework spec in section
    // 5.1.1.5 (Authentication Error Response) it says to follow OpenID spec section 3.1.2.6
    // which for starters says, "Unless the Redirection URI is invalid, the Authorization
    // Server returns the Client to the Redirection URI specified in the Authorization
    // Request with the appropriate error and state parameters. Other parameters SHOULD
    // NOT be returned."
    def onAuthFailure(error: Lti13Error): Unit = {
      val msg = error.msg
      val output = error match {
        case OAuth2LayerError(oauth2Error) =>
          val code = oauth2Error.code
          LOGGER.error(s"Authentication failed [$code]: $msg")
          s"""Authentication failed:
             |
             |Error code: $code
             |Description: $msg
             |
             |Please contact your system administrator
             |""".stripMargin
        case PlatformDetailsError(_) =>
          LOGGER.error(s"LTI 1.3 Platform configuration error: $msg")
          s"Authentication failed due to the configuration of LTI 1.3 Platform: $msg"
      }

      resp.setContentType("text/plain")
      resp.setStatus(HttpStatus.SC_FORBIDDEN)
      resp.getWriter.print(output)
    }

    val authResult: Either[Lti13Error, (Lti13Request, String)] = for {
      // Verify the ID token
      verifiedToken <- lti13tokenValidator.verifyToken(auth.state, auth.id_token)
      // Log the user in
      _ <- lti13AuthService.loginUser(wad, verifiedToken)
      // And finally determine the LTI request type
      lti13Request <- getLtiRequestDetails(verifiedToken)
    } yield (lti13Request, verifiedToken.getIssuer)

    authResult match {
      case Left(error) => onAuthFailure(error)
      case Right((ltiRequest, platformId)) =>
        ltiRequest match {
          case deepLinkingRequest: LtiDeepLinkingRequest =>
            lti13PlatformService
              .getPlatform(platformId)
              .fold(
                onAuthFailure,
                lti13IntegrationService.launchSelectionSession(deepLinkingRequest, _, req, resp)
              )

          case resourceLinkRequest: LtiResourceLinkRequest =>
            resp.sendRedirect(resp.encodeRedirectURL(resourceLinkRequest.targetLinkUri))
        }
    }

    // Finished with `state` - it's been used once, so let's dump it
    stateService.invalidateState(auth.state)
  }

  private def handleErrorResponse(
      errorResponse: AuthErrorResponse,
      resp: HttpServletResponse
  ): Unit = {
    LOGGER.error(s"Received Error Response from LTI Platform: $errorResponse")

    val output =
      s"""Received Error Response from LTI Platform:
        |
        |Error code: ${errorResponse.error.toString}
        |Description: ${errorResponse.error_description.getOrElse("None provided")}
        |
        |Please contact your system administrator.""".stripMargin

    resp.setContentType("text/plain")
    resp.setStatus(HttpStatus.SC_OK)
    resp.getWriter.print(output)

    stateService.invalidateState(errorResponse.state)
  }
}
