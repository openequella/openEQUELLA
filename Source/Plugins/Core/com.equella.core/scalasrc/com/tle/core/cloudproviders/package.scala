/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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
import java.net.URI
import java.util.UUID

package object cloudproviders {

  case class CloudOAuthCredentials(clientId: String, clientSecret: String)

  object CloudOAuthCredentials {
    def random() = CloudOAuthCredentials(UUID.randomUUID().toString, UUID.randomUUID().toString)
  }

  case class Viewer(name: String, serviceId: String)

  case class ServiceUri(authenticated: Boolean, uri: String)

  case class CloudProviderRegistration(name: String,
                                       description: Option[String],
                                       baseUrl: String,
                                       iconUrl: Option[String],
                                       providerAuth: CloudOAuthCredentials,
                                       serviceUris: Map[String, ServiceUri],
                                       viewers: Map[String, Map[String, Viewer]])

  case class CloudProviderRegistrationResponse(instance: CloudProviderInstance, forwardUrl: String)

  case class CloudProviderInstance(id: UUID,
                                   name: String,
                                   description: Option[String],
                                   baseUrl: String,
                                   iconUrl: Option[String],
                                   providerAuth: CloudOAuthCredentials,
                                   oeqAuth: CloudOAuthCredentials,
                                   serviceUris: Map[String, ServiceUri],
                                   viewers: Map[String, Map[String, Viewer]])

  case class CloudProviderDetails(id: UUID,
                                  name: String,
                                  description: Option[String],
                                  iconUrl: Option[String])
}
