package com.tle.integration.oidc

import com.dytech.edge.web.WebConstants
import com.tle.common.usermanagement.user.UserState
import com.tle.core.guice.Bind
import com.tle.integration.oidc.idp.CommonDetails
import com.tle.integration.oidc.service.{OidcAuthService, OidcConfigurationService}
import com.tle.plugins.ump.UserManagementLogonFilter
import com.tle.web.dispatcher.FilterResult
import sttp.client3.UriContext

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

    if (isLogout) {
      FilterResult.FILTER_CONTINUE
    } else {
      login(request, response)
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
    oidcConfigurationService.get.toOption
      .map(_.commonDetails)
      .filter(isSeamlessSsoEnabled) match {
      case Some(_) =>
        val logout = uri"${loggedOutURI.toString}"
        logout
          .addParam(WebConstants.NO_AUTO_LOGIN, "true")
          .toJavaUri
      case None => loggedOutURI
    }

  }

  override def logoutRedirect(loggedOutURI: URI): URI = loggedOutURI

  override def addStateParameters(
      request: HttpServletRequest,
      params: util.Map[String, Array[String]]
  ): Unit = {}

  private def isAutoLogin(request: HttpServletRequest): Boolean = {
    !Option(request.getParameterMap.get(WebConstants.NO_AUTO_LOGIN))
      .flatMap(_.headOption)
      .contains("true")
  }

  private def isSeamlessSsoEnabled(details: CommonDetails): Boolean = {
    details.enabled && details.seamlessSso
  }

  private def login(request: HttpServletRequest, response: HttpServletResponse) = {
    oidcConfigurationService.get.toOption
      .map(_.commonDetails)
      .filter(isSeamlessSsoEnabled)
      .filter(_ => isAutoLogin(request)) match {
      case Some(details) =>
        val target = Option(request.getParameterMap.get(".page")).flatMap(_.headOption).orNull

        val authUrl = authService.buildAuthUrl(
          details.authUrl.toString,
          details.authCodeClientId,
          target
        )
        response.sendRedirect(authUrl)
        new FilterResult(true)
      case None => FilterResult.FILTER_CONTINUE
    }
  }
}
