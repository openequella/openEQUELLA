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
import org.slf4j.LoggerFactory

import java.net.URI
import javax.inject.{Inject, Singleton}
import javax.servlet.http.{HttpServlet, HttpServletRequest, HttpServletResponse}
import scala.jdk.CollectionConverters._

/**
  * Handles the OpenID Connect Launch Flow as outlined in section 5.1 of the LTI 1.3 spec.
  */
@Bind
@Singleton
class OpenIDConnectLaunchServlet extends HttpServlet {
  private val LOGGER = LoggerFactory.getLogger(classOf[OpenIDConnectLaunchServlet])

  @Inject private var lti13AuthService: Lti13AuthService = _
  @Inject private var userService: UserService           = _

  override def doGet(req: HttpServletRequest, resp: HttpServletResponse): Unit = {
    LOGGER.debug("doGet() called")

    // the initial launch should also be supported by GET - the other requests
    // (e.g. AuthenticationResponse) only require POST
    InitiateLoginRequest(req.getParameterMap.asScala.toMap) match {
      case Some(initReq) => handleInitiateLoginRequest(initReq, resp)
      case None =>
        resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                       "Unsupported 'GET' LTI 1.3 launch request received.")
    }

    LOGGER.debug("doGet() complete")
  }

  override def doPost(req: HttpServletRequest, resp: HttpServletResponse): Unit = {
    LOGGER.debug("doPost() called")

    // TODO Needs validation around the request - was it a form POST request, does it have any params, etc.

    val params           = req.getParameterMap.asScala.toMap
    val processedRequest = InitiateLoginRequest(params) orElse AuthenticationResponse(params)
    // TODO: There is also one other 'request' type we need to support, and that's an 'error response'
    //       See section 4.1.2.1 of the OAuth 2.0 spec : https://datatracker.ietf.org/doc/html/rfc6749#autoid-38
    //       This can be triggered by simple things like sending the wrong 'client_id' to Moodle in the handling
    //       of the InitiateLoginRequest.

    processedRequest match {
      case Some(validRequest) =>
        validRequest match {
          case initReq: InitiateLoginRequest => handleInitiateLoginRequest(initReq, resp)
          case authResp: AuthenticationResponse =>
            handleAuthenticationResponse(authResp,
                                         userService.getWebAuthenticationDetails(req),
                                         resp)
        }
      case None =>
        resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                       "Unsupported LTI 1.3 launch request received.")
    }

    LOGGER.debug("doPost() complete")
  }

  private def handleInitiateLoginRequest(initLogin: InitiateLoginRequest,
                                         resp: HttpServletResponse): Unit = {
    LOGGER.debug("Received a request to initiate a login. Supplied values:")
    LOGGER.debug(initLogin.toString)
    lti13AuthService.buildAuthReqUrl(initLogin).map(resp.encodeRedirectURL) match {
      case Some(authRedirectUrl) => resp.sendRedirect(authRedirectUrl)
      case None =>
        resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                       "Unable to start launch with provided request.")
    }
  }

  private def handleAuthenticationResponse(auth: AuthenticationResponse,
                                           wad: WebAuthenticationDetails,
                                           resp: HttpServletResponse): Unit = {
    LOGGER.debug("Received an authentication response. Supplied values:")
    LOGGER.debug(auth.toString)

    def onAuthFailure(errorMessage: String): Unit = {
      LOGGER.error(s"Authentication failed: ${errorMessage}")
      // TODO: Can't just return SC_FORBIDDEN, need to follow the OpenID spec section 3.1.2.6
      //       which for starters says, "Unless the Redirection URI is invalid, the Authorization
      //       Server returns the Client to the Redirection URI specified in the Authorization
      //       Request with the appropriate error and state parameters. Other parameters SHOULD
      //       NOT be returned."
      resp.sendError(HttpServletResponse.SC_FORBIDDEN)
    }

    val authResult: Either[String, URI] = for {
      verificationResult <- lti13AuthService.verifyToken(auth.state, auth.id_token)
      decodedJWT    = verificationResult._1
      targetLinkUri = verificationResult._2.targetLinkUri

      userDetails <- UserDetails(decodedJWT)
      _           <- lti13AuthService.loginUser(wad, userDetails)
    } yield targetLinkUri

    authResult match {
      case Left(error)      => onAuthFailure(error)
      case Right(targetUri) => resp.sendRedirect(resp.encodeRedirectURL(targetUri.toString))
    }

    // Finished with `state` - it's been used once, so let's dump it
    Lti13StateService.invalidateState(auth.state)
  }
}
