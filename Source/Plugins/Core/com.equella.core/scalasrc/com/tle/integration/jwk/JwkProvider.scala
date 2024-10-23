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

package com.tle.integration.jwk

import com.auth0.jwk.{JwkProviderBuilder, JwkProvider => JsonWebKeySetProvider}
import com.google.common.cache.{Cache, CacheBuilder, CacheLoader}
import com.tle.beans.Institution
import com.tle.core.guice.Bind
import com.tle.core.institution.{InstitutionCache, InstitutionService}
import com.tle.integration.oauth2.ServerError
import org.slf4j.LoggerFactory
import java.net.URL
import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}
import scala.util.Try

@Bind
@Singleton
/**
  * The JWK Provider which uses [[InstitutionCache]] to cache instances of JWK Providers for each keyset URL
  * (for an institution). Doing so enables the code to benefit from the internal caching features
  * of `JwkProvider` (instead of recreating each time) while not keeping around stale instances
  * (such as if we just store them in a simple `TrieMap`.
  *
  * The key benefit being that the JWKS endpoint of the platforms will get queried a reduced number
  * of times. This also speeds up the JWT validation process - and thereby faster launches.
  */
class JwkProvider {
  private val LOGGER = LoggerFactory.getLogger(classOf[JwkProvider])

  private var jwkProviderCache: InstitutionCache[Cache[String, JsonWebKeySetProvider]] = _

  @Inject
  def setupJwkProviderCache(institutionService: InstitutionService): Unit =
    jwkProviderCache = institutionService.newInstitutionAwareCache(
      CacheLoader.from(
        (_: Institution) =>
          CacheBuilder
            .newBuilder()
            .maximumSize(10)
            .expireAfterAccess(1, TimeUnit.DAYS)
            .build[String, JsonWebKeySetProvider]()))

  def get(jwksUrl: URL): Either[ServerError, JsonWebKeySetProvider] =
    Try(
      jwkProviderCache.getCache.get(
        jwksUrl.toString,
        () => {
          LOGGER.debug(s"Creating new JwkProvider($jwksUrl)")
          // The default cache for the JwkProvider is size 5 and 10 hours, but 10 hours seems rather
          // long to wait for any issues to be resolved - so reduced to 1 hour.
          new JwkProviderBuilder(jwksUrl).cached(5, 1, TimeUnit.HOURS).build()
        }
      )).toEither.left.map(t =>
      ServerError(
        s"Failed to establish key (JWK) provider to validate JWT signature: ${t.getMessage}"))
}
