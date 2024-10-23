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
package com.tle.integration

import com.auth0.jwk.Jwk
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import com.tle.integration.oauth2.{InvalidJWT, OAuth2Error, ServerError}

import java.security.interfaces.RSAPublicKey
import scala.util.Try

package object jwt {

  /**
    * Builds a JWT verifier function that can be used to validate a JWT.
    *
    * @param jwk The token to be validated
    * @param issuer Issuer who is expected to issue the token
    * @param aud Audience who is expected to receive the token
    */
  def buildJwtVerifier(jwk: Jwk,
                       issuer: String,
                       aud: String): DecodedJWT => Either[OAuth2Error, DecodedJWT] = {
    val verifier = Try {
      // Section 5.1.3 of the Security Framework says that RS256 SHOULD be used - but there are
      // some others which are allowed as per the 'best practices'. Perhaps we should add code
      // to determine the others and use them too.
      // Best practices: https://www.imsglobal.org/spec/security/v1p1#approved-jwt-signing-algorithms
      val alg = Algorithm.RSA256(jwk.getPublicKey.asInstanceOf[RSAPublicKey], null)
      JWT
        .require(alg)
        // The issuer has kind of been validated already above - so that we could get the platform
        // ID to be able to get the JWKS URL. But we might as well explicitly validate it as part
        // of the JWT validation - as that's what you're meant to do.
        .withIssuer(issuer)
        .withAnyOfAudience(aud)
        .build()
    }.toEither.left.map(t => ServerError(s"Failed to initialise a JWT verifier: ${t.getMessage}"))

    (decodedToken: DecodedJWT) =>
      verifier.flatMap(
        v =>
          Try(v.verify(decodedToken)).toEither.left
            .map(t => InvalidJWT(s"Provided ID token (JWT) failed verification: ${t.getMessage}")))
  }
}
