package com.tle.core.securitykey.service

import com.tle.beans.securitykey._
import com.tle.core.guice.Bind
import com.tle.core.securitykey.dao.SecurityKeyDAO
import com.tle.core.securitykey.helper.SecurityKeyHelper._
import com.tle.legacy.LegacyGuice
import io.circe.syntax._
import org.springframework.transaction.annotation.Transactional
import java.security.KeyPair
import java.security.interfaces.RSAPublicKey
import java.time.Instant
import java.util.{Base64, UUID}
import javax.inject.{Inject, Singleton}

@Singleton
@Bind(classOf[SecurityKeyService])
class SecurityKeyServiceImpl extends SecurityKeyService {
  @Inject var jwkDao: SecurityKeyDAO = _

  def getKeypairByKeyID(keyId: String): Option[KeyPair] =
    jwkDao.getByKeyID(keyId).map(buildKeyPair)

  @Transactional
  def generateKeyPair: String = {
    val keyPair = generateRSAKeyPair

    val securityKey = new SecurityKey
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
