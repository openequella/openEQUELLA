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

import java.security.{MessageDigest, SecureRandom}
import java.util.Base64

package object oauth2 {

  /** Generate a pair of code challenge and code verifier to be used in the PKCE flow.
    */
  def generatePKCEPair: (String, String) = {
    def encode = Base64.getUrlEncoder.withoutPadding.encodeToString _

    val sr    = new SecureRandom
    val bytes = new Array[Byte](32)
    sr.nextBytes(bytes)
    val verifier = encode(bytes)

    val verifierBytes = verifier.getBytes("US-ASCII")
    val md            = MessageDigest.getInstance("SHA-256")
    md.update(verifierBytes, 0, verifierBytes.length)
    val digest    = md.digest
    val challenge = encode(digest)

    (verifier, challenge)
  }
}
