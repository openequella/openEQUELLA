package integtester

import integtester.testprovider.RCloudControlConfigType
import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder

package object testprovider {

  case class RCloudConfigOption(name: String, value: String)

  object RCloudControlConfigType extends Enumeration {
    val XPath, Textfield, Dropdown, Check, Radio = Value
  }

  case class RCloudControlConfig(
      id: String,
      name: String,
      description: Option[String],
      configType: RCloudControlConfigType.Value,
      options: Iterable[RCloudConfigOption],
      min: Int,
      max: Int
  )

  case class RProviderControlDefinition(
      name: String,
      iconUrl: Option[String],
      configuration: Iterable[RCloudControlConfig]
  )

  object RProviderControlDefinition {
    implicit val typeEnc: Encoder[RCloudControlConfigType.Value] =
      Encoder.enumEncoder(RCloudControlConfigType)
    implicit val ccoEnc: Encoder.AsObject[RCloudConfigOption] = deriveEncoder[RCloudConfigOption]
    implicit val enc: Encoder.AsObject[RCloudControlConfig]   = deriveEncoder[RCloudControlConfig]
    implicit val encodeControls: Encoder.AsObject[RProviderControlDefinition] =
      deriveEncoder[RProviderControlDefinition]
  }
}
