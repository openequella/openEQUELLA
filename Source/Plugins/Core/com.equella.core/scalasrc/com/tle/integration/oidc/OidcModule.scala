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
