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

import com.google.inject.multibindings.MapBinder
import com.tle.core.config.guice.OptionalConfigModule
import com.tle.core.usermanagement.{
  Auth0UserDirectory,
  EntraIdUserDirectory,
  OidcUserDirectory,
  OktaUserDirectory
}
import com.tle.integration.oidc.idp.IdentityProviderPlatform

/** Guice module for OIDC integration, providing:
  *   - binding user directories to their respective identity provider platforms.
  *   - binding a boolean configuration that controls whether OIDC token logging is enabled during
  *     the OIDC integration. The logging is disabled by default.
  */
class OidcModule extends OptionalConfigModule {
  override protected def configure(): Unit = {
    val mapBinder = MapBinder.newMapBinder(
      binder(),
      classOf[IdentityProviderPlatform.Value],
      classOf[OidcUserDirectory]
    )
    mapBinder.addBinding(IdentityProviderPlatform.ENTRA_ID).to(classOf[EntraIdUserDirectory])
    mapBinder.addBinding(IdentityProviderPlatform.AUTH0).to(classOf[Auth0UserDirectory])
    mapBinder.addBinding(IdentityProviderPlatform.OKTA).to(classOf[OktaUserDirectory])

    bindBoolean("enable.oidc.token.logging", false)
  }
}
