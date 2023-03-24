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
import org.apache.http.HttpStatus
import org.slf4j.LoggerFactory

import java.net.URI
import javax.inject.{Inject, Singleton}
import javax.servlet.http.{HttpServlet, HttpServletRequest, HttpServletResponse}
import scala.jdk.CollectionConverters._
import scala.util.Try

/**
  * Valid error codes for Error Responses as per section 4.1.2.1 (Error Response) of the RFC 6749
  * (OAuth 2).
  */
object ErrorResponseCode extends Enumeration {
  type Code = Value

  val invalid_request, unauthorized_client, access_denied, unsupported_response_type, invalid_scope,
  server_error, temporarily_unavailable = Value
}

/**
  * An Error Responses as per section 4.1.2.1 (Error Response) of the RFC 6749 (OAuth 2).
  *
  * @param error REQUIRED.  A single ASCII [USASCII] error code from {@link ErrorResponseCode}
  * @param error_description OPTIONAL.  Human-readable ASCII [USASCII] text providing additional
  *                          information, used to assist the client developer in understanding the
  *                          error that occurred. Values for the "error_description" parameter
  *                          MUST NOT include characters outside the set %x20-21 / %x23-5B / %x5D-7E.
  * @param error_uri OPTIONAL.  A URI identifying a human-readable web page with information about
  *                  the error, used to provide the client developer with additional information
  *                  about the error. Values for the "error_uri" parameter MUST conform to the
  *                  URI-reference syntax and thus MUST NOT include characters outside the set
  *                  %x21 / %x23-5B / %x5D-7E.
  * @param state REQUIRED if a "state" parameter was present in the client authorization request.
  *              The exact value received from the client.
  */
case class ErrorResponse(error: ErrorResponseCode.Code,
                         error_description: Option[String],
                         error_uri: Option[URI],
                         state: String)
object ErrorResponse {
  val PARAM_ERRORCODE   = "error"
  val PARAM_DESCRIPTION = "error_description"
  val PARAM_URI         = "error_uri"
  val PARAM_STATE       = "state"

  def apply(params: Map[String, Array[String]]): Option[ErrorResponse] = {
    val param    = getParam(params)
    val uriParam = getUriParam(param)

    for {
      error <- param(PARAM_ERRORCODE).flatMap(asString =>
        Try(ErrorResponseCode.withName(asString)).toOption)
      error_description = param(PARAM_DESCRIPTION)
      error_uri         = uriParam(PARAM_URI)
      state <- param(PARAM_STATE)
    } yield ErrorResponse(error, error_description, error_uri, state)
  }
}

/**
  * Handles the OpenID Connect Launch Flow as outlined in section 5.1 of the LTI 1.3 spec.
  */
@Bind
@Singleton
class OpenIDConnectLaunchServlet extends HttpServlet {
  private val LOGGER = LoggerFactory.getLogger(classOf[OpenIDConnectLaunchServlet])

  @Inject private var lti13AuthService: Lti13AuthService = _
  @Inject private var stateService: Lti13StateService    = _
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

    val params = req.getParameterMap.asScala.toMap
    val processedRequest = InitiateLoginRequest(params) orElse AuthenticationResponse(params) orElse ErrorResponse(
      params)

    processedRequest match {
      case Some(validRequest) =>
        validRequest match {
          case initReq: InitiateLoginRequest => handleInitiateLoginRequest(initReq, resp)
          case authResp: AuthenticationResponse =>
            handleAuthenticationResponse(authResp,
                                         userService.getWebAuthenticationDetails(req),
                                         resp)
          case errorResponse: ErrorResponse => handleErrorResponse(errorResponse, resp)
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
    stateService.invalidateState(auth.state)
  }

  private def handleErrorResponse(errorResponse: ErrorResponse, resp: HttpServletResponse): Unit = {
    LOGGER.error(s"Received Error Response from LTI Platform: ${errorResponse}")

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
