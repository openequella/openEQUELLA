package equellatests.restapi

import java.util.UUID

import org.http4s.Uri

/*
This package contains classes which represent the JSON sent to and from the cloudprovider REST API.
The 'R' prefix is to distinguish it from the server side classes of the same name.
 */
package object cloudprovider {

  val baseUri = Uri.uri("api/cloudprovider")

  case class RCloudOAuthCredentials(clientId: String, clientSecret: String)

  object RCloudOAuthCredentials {
    def random() = RCloudOAuthCredentials(UUID.randomUUID().toString, UUID.randomUUID().toString)
  }

  case class RViewer(name: String, serviceId: String)

  case class RServiceUrl(authenticated: Boolean, url: String)

  case class RCloudProviderRegistration(
      name: String,
      description: Option[String],
      baseUrl: String,
      iconUrl: Option[String],
      providerAuth: RCloudOAuthCredentials,
      serviceUrls: Map[String, RServiceUrl],
      viewers: Map[String, Map[String, RViewer]]
  )

  case class RCloudProviderRegistrationResponse(
      instance: RCloudProviderInstance,
      forwardUrl: String
  )

  case class RCloudProviderInstance(
      id: UUID,
      name: String,
      description: Option[String],
      baseUrl: String,
      iconUrl: Option[String],
      providerAuth: RCloudOAuthCredentials,
      oeqAuth: RCloudOAuthCredentials,
      serviceUrls: Map[String, RServiceUrl],
      viewers: Map[String, Map[String, RViewer]]
  )

  case class RCloudProviderForward(url: String)

}
