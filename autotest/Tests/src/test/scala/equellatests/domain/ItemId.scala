package equellatests.domain

import java.util.UUID

import io.circe.generic.semiauto._

case class ItemId(uuid: UUID, version: Int)

object ItemId {

  def random = ItemId(UUID.randomUUID(), 1)

  implicit def fromJava(oldId: com.tle.webtests.pageobject.viewitem.ItemId): ItemId =
    ItemId(UUID.fromString(oldId.getUuid), oldId.getVersion)

  implicit val itemIdEnc = deriveEncoder[ItemId]
  implicit val itemIdDec = deriveDecoder[ItemId]

}
