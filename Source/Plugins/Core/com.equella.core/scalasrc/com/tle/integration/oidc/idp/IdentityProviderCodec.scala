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

/**
  * Centralise all the required codecs for different types of IdentityProvider.
  */
object IdentityProviderCodec {
  private implicit val idpPlatformEncoder: Encoder[IdentityProviderPlatform.Value] =
    Encoder.encodeEnumeration(IdentityProviderPlatform)
  private implicit val idpPlatformDecoder: Decoder[IdentityProviderPlatform.Value] =
    Decoder.decodeEnumeration(IdentityProviderPlatform)

  private implicit val roleConfigEncoder: Encoder[RoleConfiguration] =
    deriveEncoder[RoleConfiguration]
  private implicit val roleConfigDecoder: Decoder[RoleConfiguration] =
    deriveDecoder[RoleConfiguration]

  // Because `platform` is not in the Identity Provider case class parameter list, Circe encoder will
  // not include it in the JSON output. Hence, we add `platform` manually.
  private def addPlatform(json: JsonObject, platform: IdentityProviderPlatform.Value): JsonObject =
    json.add("platform", Json.fromString(platform.toString))

  private implicit val genericIdPEncoder: Encoder.AsObject[GenericIdentityProvider] =
    deriveEncoder[GenericIdentityProvider]
      .mapJsonObject(addPlatform(_, IdentityProviderPlatform.GENERIC))

  private implicit val genericIdPDecoder: Decoder[GenericIdentityProvider] =
    deriveDecoder[GenericIdentityProvider]

  implicit val identityProviderEncoder: Encoder[IdentityProvider] = Encoder.instance {
    case generic: GenericIdentityProvider => genericIdPEncoder(generic)
    case unsupported =>
      throw new IllegalArgumentException(
        s"Unsupported OIDC Identity Provider: ${unsupported.platform}")
  }

  implicit val IdentityProviderDecoder: Decoder[IdentityProvider] = Decoder.instance { cursor =>
    cursor.downField("platform").as[IdentityProviderPlatform.Value].flatMap {
      case IdentityProviderPlatform.GENERIC => cursor.as[GenericIdentityProvider]
      case unsupported =>
        Left(
          DecodingFailure(CustomReason(s"Unsupported OIDC Identity Provider: $unsupported"), Nil))
    }
  }
}
