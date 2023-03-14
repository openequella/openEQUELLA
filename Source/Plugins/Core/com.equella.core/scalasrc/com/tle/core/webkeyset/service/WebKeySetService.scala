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

package com.tle.core.webkeyset.service

import java.security.KeyPair

trait WebKeySetService {

  /**
    * Retrieve a key pair by key ID.
    *
    * @param keyId ID of a key pair.
    * @return KeyPair matching the provided key ID,  or None if no key pair is found.
    */
  def getKeypairByKeyID(keyId: String): Option[KeyPair]

  /**
    * Generate a new key pair for the current Institution.
    *
    * @return ID of the new key pair.
    */
  def generateKeyPair: String

  /**
    * Retrieve all the key pairs of the current Institution and return a JWKS representing the them.
    *
    * @return JSON string representing the JWKS.
    */
  def generateJWKS: String

  /**
    * Find an existing key pair by key ID and deactivate it, and then create a new key pair and return its ID.
    *
    * @return ID of the new key pair, or None if the original key pair is not found.
    */
  def rotateKeyPair(keyID: String): Option[String]

  /**
    * Delete an existing key pair by key ID.
    *
    * @param keyId ID of a key pair.
    */
  def delete(keyId: String): Unit
}
