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

import com.tle.beans.webkeyset._
import com.tle.core.guice.Bind
import com.tle.core.webkeyset.dao.WebKeySetDAO
import com.tle.core.webkeyset.helper.WebKeySetHelper._
import com.tle.legacy.LegacyGuice
import io.circe.syntax._
import org.springframework.transaction.annotation.Transactional
import java.security.KeyPair
import java.security.interfaces.RSAPublicKey
import java.time.Instant
import java.util.{Base64, UUID}
import javax.inject.{Inject, Singleton}

@Singleton
@Bind(classOf[WebKeySetService])
class WebKeySetServiceImpl extends WebKeySetService {
  @Inject var jwkDao: WebKeySetDAO = _

  def getKeypairByKeyID(keyId: String): Option[KeyPair] =
    jwkDao.getByKeyID(keyId).map(buildKeyPair)

  @Transactional
  def generateKeyPair: String = {
    val keyPair = generateRSAKeyPair

    val securityKey = new WebKeySet
    securityKey.keyId = UUID.randomUUID().toString
    securityKey.algorithm = RSA
    securityKey.created = Instant.now
    securityKey.privateKey =
      LegacyGuice.encryptionService.encrypt(toPEM(keyPair.getPrivate, PRIVATE_KEY_HEADER))
    securityKey.publicKey = toPEM(keyPair.getPublic, PUBLIC_KEY_HEADER)

    jwkDao.save(securityKey)
    securityKey.keyId
  }

  @Transactional
  def delete(keyId: String): Unit = jwkDao.getByKeyID(keyId).foreach(jwkDao.delete)

  @Transactional
  def rotateKeyPair(keyID: String): String = {
    jwkDao.getByKeyID(keyID).foreach(_.deactivated = Instant.now)
    generateKeyPair
  }

  @Transactional
  def generateJWKS(kty: JWKKeyType.Value, alg: JWKAlg.Value, use: JWKUse.Value): String = {
    def base64UrlEncode(bytes: Array[Byte]): String = Base64.getUrlEncoder.encodeToString(bytes)

    val publicKey = generateRSAKeyPair.getPublic.asInstanceOf[RSAPublicKey]
    val exponent  = base64UrlEncode(publicKey.getPublicExponent.toByteArray)
    val modules   = base64UrlEncode(publicKey.getModulus.toByteArray)

    val jwk = JsonWebKey(kty = kty,
                         e = exponent,
                         n = modules,
                         kid = UUID.randomUUID().toString,
                         alg = alg,
                         use = use)

    JsonWebKeySet(jwk).asJson.spaces2
  }
}
