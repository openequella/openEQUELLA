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

import com.tle.beans.webkeyset.WebKeySet
import java.security.KeyPair

trait WebKeySetService {

  /** Retrieve a key pair by key ID.
    *
    * @param keyId
    *   ID of a key pair.
    * @return
    *   KeyPair matching the provided key ID, or None if no key pair is found.
    */
  def getKeypairByKeyID(keyId: String): Option[KeyPair]

  /** Retrieve all the key pairs, including those deactivated.
    *
    * @return
    *   A list of WebKeySet
    */
  def getAll: List[WebKeySet]

  /** Generate a new key pair for the current Institution.
    *
    * @return
    *   The new key pair.
    */
  def generateKeyPair: WebKeySet

  /** Retrieve all the key pairs of the current Institution and return a JWKS representing the them.
    *
    * @return
    *   JSON string representing the JWKS.
    */
  def generateJWKS: String

  /** Deactivate the provided key pair and then create a new one.
    *
    * @return
    *   The new activated key pair.
    */
  def rotateKeyPair(activatedKeyPair: WebKeySet): WebKeySet

  /** Delete an existing key pair by key ID.
    *
    * @param keyId
    *   ID of a key pair.
    */
  def delete(keyId: String): Unit

  /** Delete all the key pairs for the current Institution.
    */
  def deleteAll(): Unit

  /** Save the provided key pair for the current Institution. If it exists already, then update it.
    *
    * @param keySet
    *   Key pair to be either saved or updated.
    */
  def createOrUpdate(keySet: WebKeySet): Unit
}
