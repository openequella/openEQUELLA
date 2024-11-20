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

package com.tle.integration.oidc.idp

import io.circe.DecodingFailure.Reason.CustomReason
import io.circe.{Decoder, DecodingFailure, Encoder, Json, JsonObject}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

import java.net.{URI, URL}
import scala.util.Try

/**
  * Centralise all the required codecs for different types of IdentityProvider.
  */
object IdentityProviderCodec {
  private implicit val uriEncoder = Encoder.encodeString.contramap[URL](_.toString)
  private implicit val uriDecoder = Decoder.decodeString.emapTry(s => Try(URI.create(s).toURL))

  private implicit val idpPlatformEncoder: Encoder[IdentityProviderPlatform.Value] =
    Encoder.encodeEnumeration(IdentityProviderPlatform)
  private implicit val idpPlatformDecoder: Decoder[IdentityProviderPlatform.Value] =
    Decoder.decodeEnumeration(IdentityProviderPlatform)

  private implicit val roleConfigEncoder: Encoder[RoleConfiguration] =
    deriveEncoder[RoleConfiguration]
  private implicit val roleConfigDecoder: Decoder[RoleConfiguration] =
    deriveDecoder[RoleConfiguration]

  private implicit val commonDetailsEncoder: Encoder.AsObject[CommonDetails] =
    deriveEncoder[CommonDetails]

  private implicit val commonDetailsDecoder: Decoder[CommonDetails] =
    deriveDecoder[CommonDetails]

  private implicit val genericIdPDetailsEncoder: Encoder.AsObject[GenericIdentityProviderDetails] =
    deriveEncoder[GenericIdentityProviderDetails]

  private implicit val genericIdPDetailsDecoder: Decoder[GenericIdentityProviderDetails] =
    deriveDecoder[GenericIdentityProviderDetails]

  implicit val idpDetailsEncoder: Encoder[IdentityProviderDetails] = Encoder.instance {
    case generic: GenericIdentityProviderDetails => genericIdPDetailsEncoder(generic)
    case unsupported =>
      throw new IllegalArgumentException(
        s"Unsupported OIDC Identity Provider: ${unsupported.commonDetails.platform}")
  }

  implicit val idpDetailsDecoder: Decoder[IdentityProviderDetails] = Decoder.instance { cursor =>
    cursor
      .downField("commonDetails")
      .downField("platform")
      .as[IdentityProviderPlatform.Value]
      .flatMap {
        case IdentityProviderPlatform.AUTH0 | IdentityProviderPlatform.ENTRA_ID =>
          cursor.as[GenericIdentityProviderDetails]
        case unsupported =>
          Left(
            DecodingFailure(CustomReason(s"Unsupported OIDC Identity Provider: $unsupported"), Nil))
      }
  }
}
