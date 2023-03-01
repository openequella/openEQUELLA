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

package com.tle.core.securitykey.service

import com.tle.beans.securitykey.{JWKAlg, JWKKeyType, JWKUse, JsonWebKeySet}

import java.security.KeyPair

trait SecurityKeyService {

  /**
    * Retrieve a key pair by key ID.
    *
    * @param keyId ID of a key pair.
    * @return KeyPair matching the provided key ID,  or None if no key pair is found.
    */
  def getKeypairByKeyID(keyId: String): Option[KeyPair]

  /**
    * Generate a new key pair.
    *
    * @return ID of the new key pair.
    */
  def generateKeyPair: String

  /**
    * Generate a new key pair and then use the key pair to create a Json Web Key Set.
    *
    * @param kty Key type to indicate what cryptographic algorithm is used to generate the key pair. Default to RSA.
    * @param alg Algorithm used to secure the content of a JWT. Default to RS256.
    * @param use The intended use of the key which is for either signature or encryption. Default to signature.
    *
    * @return JSON string representing the new JWKS.
    */
  def generateJWKS(kty: JWKKeyType.Value = JWKKeyType.RSA,
                   alg: JWKAlg.Value = JWKAlg.RS256,
                   use: JWKUse.Value = JWKUse.sig): String

  /**
    * Find an existing key pair by key ID and deactivate it, and then create a new key pair.
    *
    * @return JSON string representing the JWKS which has a new key added to the end of the set.
    */
  def rotateKeyPair(keyID: String): String

  /**
    * Delete an existing key pair.
    *
    * @param keyId ID of a key pair.
    */
  def delete(keyId: String): Unit

}
