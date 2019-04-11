package equellatests.restapi.cloudprovider

import equellatests.restapi.ERest

object RCloudProviders {

  import io.circe.generic.auto._

  def initCallback(url: String): ERest[RCloudProviderForward] = {
    ERest.postEmpty[RCloudProviderForward](baseUri / "register" / "init", Map("url" -> Seq(url)))
  }
}
