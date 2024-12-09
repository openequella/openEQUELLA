package equellatests.restapi

import java.util.UUID

import cats.data.OptionT
import equellatests.domain.ItemId
import io.circe.generic.auto._
import org.http4s.headers.Location
import org.http4s.util.CaseInsensitiveString
import org.http4s.{Status, Uri}

object RItems {

  private val IdFromUriRegex = """/(.*)/(.*)/""".r

  val baseUri = Uri.uri("api/item")

  def itemUri(i: ItemId) = baseUri / i.uuid.toString / i.version.toString

  def get(i: ItemId): ERest[RItem] = {
    ERest.get(itemUri(i))
  }

  def getModeration(i: ItemId): ERest[RModeration] = {
    ERest.get(itemUri(i) / "moderation")
  }

  def create(item: RCreateItem): ERest[ItemId] = {
    (for {
      fullUri <- OptionT[ERest, Uri](ERest.postCheckHeaders(baseUri, item).map {
        case (Status.Created, h) => h.get(Location).map(_.uri)
        case _                   => None
      })
      idUri <- OptionT(ERest.relative(fullUri, baseUri))
    } yield idUri.path match {
      case IdFromUriRegex(uuid, version) => new ItemId(UUID.fromString(uuid), version.toInt)
    }).getOrElse(sys.error("OOPS"))
  }

  def getHistory(i: ItemId): ERest[Seq[RHistoryEvent]] = {
    ERest.get(itemUri(i) / "history")
  }
}
