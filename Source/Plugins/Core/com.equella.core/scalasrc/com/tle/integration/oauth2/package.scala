package com.tle.integration

import java.security.{MessageDigest, SecureRandom}
import java.util.Base64

package object oauth2 {

  /**
    * Generate a pair of code challenge and code verifier to be used in the PKCE flow.
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
