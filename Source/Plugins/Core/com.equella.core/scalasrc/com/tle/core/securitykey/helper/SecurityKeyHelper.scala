package com.tle.core.securitykey.helper

import com.tle.beans.securitykey.SecurityKey
import com.tle.legacy.LegacyGuice
import org.bouncycastle.util.io.pem.{PemObject, PemReader, PemWriter}
import java.io.{StringReader, StringWriter}
import java.security.spec.{PKCS8EncodedKeySpec, X509EncodedKeySpec}
import java.security.{Key, KeyFactory, KeyPair, KeyPairGenerator}

object SecurityKeyHelper {
  val PUBLIC_KEY_HEADER  = "PUBLIC KEY"
  val PRIVATE_KEY_HEADER = "PRIVATE KEY"
  val RSA                = "RSA"
  val KEY_SIZE           = 2048

  /**
    * Convert a Key into a string in X.509 PEM format.
    *
    * @param key Either a public key or private key.
    * @param header Header to be used in the PEM format.
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

  /**
    * Given a SecurityKey, convert the X.509 PEM format strings into a private key and a public key
    * and return the key pair.
    *
    * @param key Instance of SecurityKey where the private and public keys are saved X.509 PEM format strings.
    */
  def buildKeyPair(key: SecurityKey): KeyPair = {
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

  /**
    * Generate a new key pair. The cryptographic algorithm is RSA and the key size is 2048.
    */
  def generateRSAKeyPair: KeyPair = {
    val generator = KeyPairGenerator.getInstance(RSA)
    generator.initialize(KEY_SIZE)
    generator.generateKeyPair()
  }
}
