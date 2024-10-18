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

import com.auth0.jwt.interfaces.DecodedJWT
import com.tle.integration.oauth2.InvalidJWT
import scala.jdk.CollectionConverters._
import cats.implicits._

package object oidc {

  /**
    * From the provided decoded JWT return the specified claim which is expected to be a string.
    *
    * @param jwt   a token containing the claim
    * @param claim name of a string based claim
    * @return If available will return the string value of the claim, or `None`
    */
  def getClaim(jwt: DecodedJWT, claim: String): Option[String] =
    Option(jwt.getClaim(claim)).flatMap(c => Option(c.asString()))

  /**
    * Return the specified claim which must be present in the provided decoded JWT as a string.
    *
    * @param jwt   a token containing the claim
    * @param claim name of a string based claim
    * @return The string value of the claim, or `InvalidJWT` if the claim is absent.
    */
  def getRequiredClaim(jwt: DecodedJWT, claim: String): Either[InvalidJWT, String] =
    Option(jwt.getClaim(claim))
      .flatMap(c => Option(c.asString()))
      .toRight(InvalidJWT(s"Failed to extract $claim from JWT"))

  /**
    * For a claim where type of the value is non-textual, use this method to get the string representation
    * of the claim value.
    *
    * @param jwt a token containing the claim
    * @param claim name of a string based claim
    * @return If available will return the string representation of the claim, or `None`
    */
  def getClaimStringRepr(jwt: DecodedJWT, claim: String): Option[String] =
    Option(jwt.getClaim(claim)).flatMap(c => Option(c.toString))

  /**
    * Get value of a claim as a Map.
    *
    * @param jwt a token containing the claim
    * @param claim name of a string based claim
    * @return If transforming the value to a Map is successful return the Map, or `None`
    */
  def getClaimAsMap(jwt: DecodedJWT, claim: String): Option[Map[String, AnyRef]] =
    Option(jwt.getClaim(claim))
    // `asMap` may return null or throw JWTDecodeException.
      .map(c => Either.catchNonFatal(Option(c.asMap)))
      .flatMap(_.toOption)
      .flatten
      .map(_.asScala.toMap)

  /**
    * Given a decoded JWT will return a partially applied function which can then receive the name
    * of a claim and return the value as a `String` or `None` if not present in the claims.
    *
    * @param jwt a token containing claims which will be wrapped in the returned function
    * @return a function which given the name of a claim will optionally return its value.
    */
  def getClaim(jwt: DecodedJWT): String => Option[String] =
    (claim: String) => getClaim(jwt, claim)
}
