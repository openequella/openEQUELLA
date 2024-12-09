/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0, (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.core
import java.util.UUID
import com.tle.beans.Institution
import com.tle.common.usermanagement.user.AbstractUserState
import com.tle.common.usermanagement.user.valuebean.DefaultUserBean
import com.tle.core.security.ACLChecks.hasAclOrThrow
import com.tle.web.cloudprovider.CloudProviderConstants
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto._

package object cloudproviders {

  case class CloudOAuthCredentials(clientId: String, clientSecret: String)

  object CloudOAuthCredentials {
    def random() = CloudOAuthCredentials(UUID.randomUUID().toString, UUID.randomUUID().toString)

    implicit val decodeCreds: Decoder[CloudOAuthCredentials] = deriveDecoder
  }

  case class Viewer(name: String, serviceId: String)

  object Viewer {
    implicit val decodeViewer: Decoder[Viewer] = deriveDecoder
  }

  case class ServiceUrl(url: String, authenticated: Boolean)

  object ServiceUrl {
    implicit val decodeServiceUri: Decoder[ServiceUrl] = deriveDecoder
  }

  case class CloudProviderRegistration(
      name: String,
      description: Option[String],
      vendorId: String,
      baseUrl: String,
      iconUrl: Option[String],
      providerAuth: CloudOAuthCredentials,
      serviceUrls: Map[String, ServiceUrl],
      viewers: Map[String, Map[String, Viewer]]
  )

  case class CloudProviderRegistrationResponse(instance: CloudProviderInstance, forwardUrl: String)

  case class CloudProviderInstance(
      id: UUID,
      name: String,
      description: Option[String],
      vendorId: String,
      baseUrl: String,
      iconUrl: Option[String],
      providerAuth: CloudOAuthCredentials,
      oeqAuth: CloudOAuthCredentials,
      serviceUrls: Map[String, ServiceUrl],
      viewers: Map[String, Map[String, Viewer]]
  )

  case class CloudProviderDetails(
      id: UUID,
      name: String,
      description: Option[String],
      iconUrl: Option[String],
      vendorId: String,
      canRefresh: Boolean
  )

  case class CloudProviderData(
      baseUrl: String,
      iconUrl: Option[String],
      vendorId: String,
      providerAuth: CloudOAuthCredentials,
      oeqAuth: CloudOAuthCredentials,
      serviceUrls: Map[String, ServiceUrl],
      viewers: Map[String, Map[String, Viewer]]
  )

  /** Object to provide Circe encoders and decoders for the types of CloudProviderData, Viewer,
    * ServiceUrl and CloudOAuthCredentials.
    */
  object CloudProviderData {
    implicit val decoderV = deriveDecoder[Viewer]
    implicit val encoderV = deriveEncoder[Viewer]
    implicit val decoderS = deriveDecoder[ServiceUrl]
    implicit val encoderS = deriveEncoder[ServiceUrl]
    implicit val decoderC = deriveDecoder[CloudOAuthCredentials]
    implicit val encoderC = deriveEncoder[CloudOAuthCredentials]
    implicit val decoder  = deriveDecoder[CloudProviderData]
    implicit val encoder  = deriveEncoder[CloudProviderData]
  }

  case class CloudProviderRefreshRequest(id: UUID)

  object CloudProviderRefreshRequest {
    implicit val encoder: Encoder[CloudProviderRefreshRequest] = deriveEncoder
  }
  object CloudProviderRegistration {
    implicit val decoder: Decoder[CloudProviderRegistration] = deriveDecoder
  }

  class CloudProviderUserState(val providerId: UUID, institution: Institution)
      extends AbstractUserState {

    val userId = "cp:" + providerId
    setInstitution(institution)
    setLoggedInUser(new DefaultUserBean(userId, userId, "Cloud", "Provider", null))

    override def isSystem: Boolean = true
    override def getSessionID      = "PROVIDER"
  }

  def checkPermissions(): Unit = {
    hasAclOrThrow(CloudProviderConstants.PRI_MANAGE_CLOUD_PROVIDER)
  }
}
