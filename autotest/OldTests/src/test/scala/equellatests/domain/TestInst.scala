package equellatests.domain

import java.io.File
import java.net.URI

import io.circe.generic.semiauto._
import JsonCodecs._

case class TestInst(baseUri: URI, systemPassword: String, baseFolderName: String)

object TestInst {
  implicit val testInstEnc = deriveEncoder[TestInst]
  implicit val testInstDec = deriveDecoder[TestInst]
}
