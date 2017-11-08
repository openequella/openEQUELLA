package equellatests.restapi

import java.util.UUID

import io.circe.{Decoder, Encoder}

object RStatus extends Enumeration {
  type RStatus = Value
  val live, moderating = Value
  implicit val encRStatus : Encoder[RStatus] = Encoder.enumEncoder(RStatus)
  implicit val decRStatus : Decoder[RStatus] = Decoder.enumDecoder(RStatus)
}

import RStatus._

case class RItem(uuid: UUID, version: Int, name: Option[String], metadata: String, status: RStatus,
                 attachments: Seq[BasicAttachment])

sealed trait RAttachment

case class BasicAttachment(description: String, viewer: String, restricted: Boolean) extends RAttachment
{
  def viewerO : Option[String] = Some(viewer).filterNot(_.isEmpty)
}