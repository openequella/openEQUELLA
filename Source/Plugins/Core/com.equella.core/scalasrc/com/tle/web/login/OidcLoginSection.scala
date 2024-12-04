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

package com.tle.web.login

import com.tle.core.guice.Bind
import com.tle.core.i18n.CoreStrings
import com.tle.integration.oidc.idp.IdentityProviderDetails
import com.tle.integration.oidc.service._
import com.tle.web.freemarker.FreemarkerFactory
import com.tle.web.freemarker.annotations.ViewFactory
import com.tle.web.sections.annotations.{EventFactory, EventHandlerMethod}
import com.tle.web.sections.events.RenderEventContext
import com.tle.web.sections.events.js.EventGenerator
import com.tle.web.sections.render.{HtmlRenderer, TextLabel}
import com.tle.web.sections.standard.annotations.Component
import com.tle.web.sections.standard.{AbstractHtmlComponent, Button}
import com.tle.web.sections.{SectionInfo, SectionResult}
import javax.inject.Inject

/** This Section is responsible for rendering the OIDC login button and handling the login process.
  * The login is performed through OAuth 2 PKCE flow.
  */
@Bind
class OidcLoginSection extends AbstractHtmlComponent[Unit] with HtmlRenderer {

  @ViewFactory private var viewFactory: FreemarkerFactory = _

  @EventFactory private var events: EventGenerator = _

  @Component private var loginButton: Button = _

  @Inject
  private var oidcConfigurationService: OidcConfigurationService = _

  @Inject
  private var authService: OidcAuthService = _

  @EventHandlerMethod
  def launch(info: SectionInfo, authUrl: String, clientId: String, targetPage: String): Unit =
    info.forwardToUrl(authService.buildAuthUrl(authUrl, clientId, targetPage))

  override def renderHtml(context: RenderEventContext): SectionResult = {
    def targetPage(ctx: RenderEventContext) = {
      val logonSection: LogonSection = ctx.lookupSection(classOf[LogonSection])
      logonSection.getModel(context).getPage
    }

    def renderLoginButton(idp: IdentityProviderDetails) = {

      loginButton.setLabel(context, new TextLabel(CoreStrings.text("login.oidc.button")))
      loginButton.setClickHandler(
        context,
        events.getNamedHandler(
          "launch",
          idp.commonDetails.authUrl,
          idp.commonDetails.authCodeClientId,
          targetPage(context)
        )
      )
      viewFactory.createResult("logon/oidclogin.ftl", context)
    }

    // If OIDC support is enabled, render the SSO section. Otherwise
    // return `null` to ensure it remains hidden.
    oidcConfigurationService.get.toOption
      .filter(_.commonDetails.enabled)
      .map(renderLoginButton)
      .orNull
  }

  override def getModelClass: Class[Unit] = classOf[Unit]

  override def instantiateModel(info: SectionInfo): Unit = ()

  // This method is required by the ftl template to access the component.
  def getLoginButton: Button = loginButton

}
