package equellatests.restapi

import equellatests.domain.ItemId
import io.circe.generic.auto._
import org.http4s.Uri

object RItems {

  def get(i: ItemId): ERest[RItem] = {
    ERest.get(Uri.uri("api/item") / i.uuid.toString / i.version.toString)
  }
}
