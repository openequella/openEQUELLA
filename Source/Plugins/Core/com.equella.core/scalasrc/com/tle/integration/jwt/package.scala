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
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import com.tle.integration.oauth2.error.general.InvalidJWT
import java.security.PublicKey
import java.security.interfaces.{ECPublicKey, RSAPublicKey}

package object jwt {

  private object SigningAlg extends Enumeration {
    val RS256, RS384, RS512, ES256, ES384, ES512 = Value
  }

  // Default to RSA256 which is the most commonly used one.
  private def defaultAlgorithm(publicKey: PublicKey) =
    Algorithm.RSA256(publicKey.asInstanceOf[RSAPublicKey], null)

  // Use the value of claim 'alg' to confirm the algorithm used for signature verification.
  def determineAlg(alg: String, publicKey: PublicKey): Algorithm =
    Either
      .catchNonFatal(SigningAlg.withName(alg))
      .map {
        case SigningAlg.RS256 => defaultAlgorithm(publicKey)
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
      .getOrElse(defaultAlgorithm(publicKey))

  /**
    * Decode the supplied raw JWT, and return the decoded token, or return InvalidJWT if the decode fails.
    */
  def decodeJwt(rawToken: String): Either[InvalidJWT, DecodedJWT] =
    Either
      .catchNonFatal(JWT.decode(rawToken))
      .leftMap(t => InvalidJWT(s"Failed to decode token: ${t.getMessage}"))
}
