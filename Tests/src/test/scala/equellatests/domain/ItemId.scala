package equellatests.domain

import java.util.UUID

case class ItemId(uuid: UUID, version: Int)

object ItemId {
  implicit def fromJava(oldId: com.tle.webtests.pageobject.viewitem.ItemId): ItemId =
    ItemId(UUID.fromString(oldId.getUuid), oldId.getVersion)
}

