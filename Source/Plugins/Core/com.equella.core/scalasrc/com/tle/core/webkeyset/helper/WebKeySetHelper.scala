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

package com.tle.core.webkeyset.helper

import com.tle.beans.webkeyset.WebKeySet
import com.tle.legacy.LegacyGuice
import org.bouncycastle.util.io.pem.{PemObject, PemReader, PemWriter}
import java.io.{StringReader, StringWriter}
import java.security.spec.{PKCS8EncodedKeySpec, X509EncodedKeySpec}
import java.security.{Key, KeyFactory, KeyPair, KeyPairGenerator}

object WebKeySetHelper {
  val PUBLIC_KEY_HEADER  = "PUBLIC KEY"
  val PRIVATE_KEY_HEADER = "PRIVATE KEY"
  val RSA                = "RSA"
  val KEY_SIZE           = 2048

  /** Convert a Key into a string in X.509 PEM format.
    *
    * @param key
    *   Either a public key or private key.
    * @param header
    *   Header to be used in the PEM format.
    */
  def toPEM(key: Key, header: String): String = {
    val sw = new StringWriter
    val pw = new PemWriter(sw)
    pw.writeObject(new PemObject(header, key.getEncoded))
    pw.flush()
    pw.close()
    sw.flush()
    sw.toString
  }

  /** Given a WebKeySet, convert the X.509 PEM format strings into a private key and a public key
    * and return the key pair.
    *
    * @param key
    *   Instance of SecurityKey where the private and public keys are saved X.509 PEM format
    *   strings.
    */
  def buildKeyPair(key: WebKeySet): KeyPair = {
    def getPemContent(key: String) = {
      val pemReader = new PemReader(new StringReader(key))
      val pemObject = pemReader.readPemObject()
      pemObject.getContent
    }

    val factory = KeyFactory.getInstance(key.algorithm)

    val decryptedPrivateKey = LegacyGuice.encryptionService.decrypt(key.privateKey)
    val originalPrivateKey =
      factory.generatePrivate(new PKCS8EncodedKeySpec(getPemContent(decryptedPrivateKey)))

    val publicKey = factory.generatePublic(new X509EncodedKeySpec(getPemContent(key.publicKey)))

    new KeyPair(publicKey, originalPrivateKey)
  }

  /** Generate a new key pair. The cryptographic algorithm is RSA and the key size is 2048.
    */
  def generateRSAKeyPair: KeyPair = {
    val generator = KeyPairGenerator.getInstance(RSA)
    generator.initialize(KEY_SIZE)
    generator.generateKeyPair()
  }
}
