package equellatests.pages.cloudprovider
import io.circe.generic.semiauto._

case class TestCloudProviderDetails(
    name: String,
    description: Option[String],
    iconUrl: Option[String]
)

object TestCloudProviderDetails {
  implicit val enc = deriveEncoder[TestCloudProviderDetails]
  implicit val dec = deriveDecoder[TestCloudProviderDetails]
}
