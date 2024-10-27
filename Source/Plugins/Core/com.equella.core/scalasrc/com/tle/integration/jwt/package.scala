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

import cats.implicits._
import com.auth0.jwk.Jwk
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import com.auth0.jwt.{JWT, JWTVerifier}
import com.tle.integration.oauth2.{InvalidJWT, OAuth2Error, ServerError}
import java.security.PublicKey
import java.security.interfaces.{ECPublicKey, RSAPublicKey}
import scala.util.Try

package object jwt {

  private object SigningAlg extends Enumeration {
    val RS256, RS384, RS512, ES256, ES384, ES512 = Value
  }

  private def determineAlg(alg: String, publicKey: PublicKey): Either[ServerError, Algorithm] =
    Either
      .catchNonFatal(SigningAlg.withName(alg))
      .map {
        case SigningAlg.RS256 =>
          Algorithm.RSA256(publicKey.asInstanceOf[RSAPublicKey], null)
        case SigningAlg.RS384 =>
          Algorithm.RSA384(publicKey.asInstanceOf[RSAPublicKey], null)
        case SigningAlg.RS512 =>
          Algorithm.RSA512(publicKey.asInstanceOf[RSAPublicKey], null)
        case SigningAlg.ES256 =>
          Algorithm.ECDSA256(publicKey.asInstanceOf[ECPublicKey], null)
        case SigningAlg.ES384 =>
          Algorithm.ECDSA384(publicKey.asInstanceOf[ECPublicKey], null)
        case SigningAlg.ES512 =>
          Algorithm.ECDSA512(publicKey.asInstanceOf[ECPublicKey], null)
      }
      .leftMap(_ => ServerError(s"Unsupported algorithm: $alg"))

  /**
    * Builds a JWT verifier function that can be used to validate a JWT.
    *
    * @param jwk JWK used to verify the signature of JWTs
    * @param issuer Issuer who is expected to issue the token
    * @param aud Audience who is expected to receive the token
    * @param alg Algorithm used for signature verification
    */
  def buildJwtVerifier(jwk: Jwk,
                       issuer: String,
                       aud: String,
                       alg: String): DecodedJWT => Either[OAuth2Error, DecodedJWT] = {

    def jwtVerifier: Either[ServerError, JWTVerifier] =
      for {
        algorithm <- determineAlg(alg, jwk.getPublicKey)
        verifier <- Either
          .catchNonFatal(
            JWT
              .require(algorithm)
              .withIssuer(issuer)
              .withAnyOfAudience(aud)
              .build())
          .leftMap(t => ServerError(s"Failed to initialise a JWT verifier: ${t.getMessage}"))
      } yield verifier

    (decodedToken: DecodedJWT) =>
      jwtVerifier.flatMap(
        v =>
          Try(v.verify(decodedToken)).toEither.left
            .map(t => InvalidJWT(s"Provided JWT failed verification: ${t.getMessage}")))
  }
}
