package com.tle.beans.securitykey

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

case class JsonWebKey(kty: JWKKeyType.Value,
                      e: String,
                      use: JWKUse.Value,
                      kid: String,
                      alg: JWKAlg.Value,
                      n: String)

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
