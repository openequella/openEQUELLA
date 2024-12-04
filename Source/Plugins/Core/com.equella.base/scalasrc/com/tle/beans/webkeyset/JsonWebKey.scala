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

package com.tle.beans.webkeyset

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

object JWKKeyType extends Enumeration {
  val RSA = Value
}
object JWKUse extends Enumeration {
  val sig, enc = Value
}

object JWKAlg extends Enumeration {
  val RS256, RS384, RS512 = Value
}

/** Model class for the structure of a public key in JWK format
  *
  * @param kty
  *   The cryptographic algorithm used with the key, such as "RSA".
  * @param e
  *   Exponent of the public key represented as a Base64urlUInt-encoded value.
  * @param use
  *   The intended use of the key which is either Signature or Encryption.
  * @param kid
  *   Unique ID of the key.
  * @param alg
  *   The algorithm intended for use with the key.
  * @param n
  *   Modules of the public key represented as a Base64urlUInt-encoded value.
  */
case class JsonWebKey(
    kty: JWKKeyType.Value,
    e: String,
    use: JWKUse.Value,
    kid: String,
    alg: JWKAlg.Value,
    n: String
)

case class JsonWebKeySet(keys: Array[JsonWebKey])

object JsonWebKey {
  implicit val jwkKeyUseDecoder = Decoder.decodeEnumeration(JWKUse)
  implicit val jwkKeyUseEncoder = Encoder.encodeEnumeration(JWKUse)

  implicit val jwkKeyTypeDecoder = Decoder.decodeEnumeration(JWKKeyType)
  implicit val jwkKeyTypeEncoder = Encoder.encodeEnumeration(JWKKeyType)

  implicit val jwkAlgDecoder = Decoder.decodeEnumeration(JWKAlg)
  implicit val jwkAlgEncoder = Encoder.encodeEnumeration(JWKAlg)

  implicit val jsonWebKeyEncoder = deriveEncoder[JsonWebKey]
  implicit val jsonWebKeyDecoder = deriveDecoder[JsonWebKey]

}

object JsonWebKeySet {
  implicit val jsonWebKeySetEncoder = deriveEncoder[JsonWebKeySet]
  implicit val jsonWebKeySetDecoder = deriveDecoder[JsonWebKeySet]

  def apply(jwk: JsonWebKey): JsonWebKeySet = JsonWebKeySet(Array(jwk))
}
