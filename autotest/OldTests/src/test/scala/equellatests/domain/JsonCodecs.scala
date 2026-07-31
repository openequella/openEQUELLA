package equellatests.domain

import java.net.URI

import io.circe.{Decoder, Encoder}

object JsonCodecs {
  implicit val uriEncoder = Encoder.encodeString.contramap[URI](_.toString)
  implicit val uriDecoder = Decoder.decodeString.map(URI.create)
}
