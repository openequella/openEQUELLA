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

package com.tle.integration.lti13

import com.auth0.jwt.interfaces.DecodedJWT
import com.tle.integration.lti13.Lti13Claims.CUSTOM_PARAMETERS
import com.tle.integration.lti13.LtiMessageType.MessageType
import cats.implicits._
import com.tle.integration.oauth2.{InvalidJWT, OAuth2Error}
import com.tle.integration.oidc.{
  OpenIDConnectParams,
  getClaimAsMap,
  getClaimStringRepr,
  getRequiredClaim
}
import io.circe.Decoder
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto.deriveConfiguredDecoder
import io.circe.generic.semiauto.deriveDecoder
import io.circe.parser.decode
import java.net.URL
import scala.jdk.CollectionConverters._

/**
  * Data structure for LTI 1.3 deep linking settings as per <https://www.imsglobal.org/spec/lti-dl/v2p0#deep-linking-settings>.
  *
  * @param deepLinkReturnUrl URL where the tool redirects the user back to the platform.
  * @param acceptTypes A list of resource types accepted such as "link" and "ltiResourceLink".
  *                    See <https://www.imsglobal.org/spec/lti-dl/v2p0#content-item-types> for more accepted types.
  * @param acceptPresentationDocumentTargets A list of supported document targets (e.g. `iframe` & window`).
  * @param acceptMediaTypes A list of accepted media types. Only applies to File types.
  * @param acceptMultiple Whether selecting multiple resources in a single response is allowed.
  * @param acceptLineItem Whether the platform supports line items.
  * @param autoCreate Whether to persist the selected resource in the platform.
  * @param title Default text for the selected resource.
  * @param text Default description for the selected resources.
  * @param data An opaque value which must be included in the response if it's present in the request.
  */
case class LtiDeepLinkingSettings(
    deepLinkReturnUrl: URL,
    acceptTypes: Array[String],
    acceptPresentationDocumentTargets: Array[String],
    acceptMediaTypes: Option[String],
    acceptMultiple: Option[Boolean],
    acceptLineItem: Option[Boolean],
    autoCreate: Option[Boolean],
    title: Option[String],
    text: Option[String],
    data: Option[String]
)

object LtiDeepLinkingSettings {
  // Name of each settings is defined in the format of snake case so add this config to help decode.
  implicit val config: Configuration = Configuration.default.withSnakeCaseMemberNames
  implicit val urlDecoder: Decoder[URL] = Decoder.decodeString.emap { str =>
    Either
      .catchNonFatal(new URL(str))
      .leftMap(error => s"Failed to decode URL: ${error.getMessage}")
  }
  implicit val decoder: Decoder[LtiDeepLinkingSettings] = deriveConfiguredDecoder

  private def decodeDeepLinkingSettings(
      settings: String): Either[InvalidJWT, LtiDeepLinkingSettings] =
    decode[LtiDeepLinkingSettings](settings)
      .leftMap(error => InvalidJWT(s"Failed to decode deep linking settings: ${error.getMessage}"))

  def apply(jwt: DecodedJWT): Either[InvalidJWT, LtiDeepLinkingSettings] = {
    getClaimStringRepr(jwt, Lti13Claims.DEEP_LINKING_SETTINGS)
      .toRight(InvalidJWT(s"Failed to extract Deep linking settings from JWT"))
      .flatMap(decodeDeepLinkingSettings)
  }
}

/**
  * Data structure for LTI 1.3 deep linking context claim (`https://purl.imsglobal.org/spec/lti/claim/context`) as per <https://www.imsglobal.org/spec/lti/v1p3#context-claim>.
  *
  * @param id Stable identifier that uniquely identifies the context from which the LTI message initiates.
  * @param `type` An array of URI values for context types. If present, the array MUST include at least one
  *               context type from the context type vocabulary described in <https://www.imsglobal.org/spec/lti/v1p3#context-type-vocabulary>
  * @param label Short descriptive name for the context. This often carries the "course code" for a course offering or course section context.
  * @param title Full descriptive name for the context. This often carries the "course title" or "course name" for a course offering context.
  */
case class LtiDeepLinkingContext(id: String,
                                 `type`: Option[Array[String]],
                                 label: Option[String],
                                 title: Option[String])

object LtiDeepLinkingContext {
  implicit val decoder: Decoder[LtiDeepLinkingContext] = deriveDecoder

  def apply(jwt: DecodedJWT): Option[LtiDeepLinkingContext] =
    getClaimStringRepr(jwt, Lti13Claims.CONTEXT).flatMap(decode(_).toOption)

}

object LtiMessageType extends Enumeration {
  type MessageType = Value
  val LtiDeepLinkingRequest, LtiDeepLinkingResponse, LtiResourceLinkRequest = Value
}

sealed trait Lti13Request {
  val messageType: MessageType
  val version: String = "1.3.0"
}

/**
  * Data structure for LTI 1.3 deep linking request as per <https://www.imsglobal.org/spec/lti-dl/v2p0#deep-linking-request-message>
  * and <https://www.imsglobal.org/spec/security/v1p0/#tool-jwt>
  *
  * @param iss Issuer Identifier for the Issuer of the message.
  * @param aud Audience(s) for whom this Tool JWT is intended.
  * @param nonce A case-sensitive string value used to associate a Tool session with a Tool JWT.
  * @param deploymentId The claim identifies the platform-tool integration governing the message.
  * @param deepLinkingSettings The claim that composes properties that characterize the kind of deep linking request the platform user is making.
  * @param customParams Optional claim that provides a key-value map of defined custom properties. The values must be strings.
  * @param context Optional claim that composes properties for the platform context from within which the deep linking request occurs.
  */
case class LtiDeepLinkingRequest(iss: String,
                                 aud: String,
                                 nonce: String,
                                 deploymentId: String,
                                 deepLinkingSettings: LtiDeepLinkingSettings,
                                 customParams: Option[Map[String, String]],
                                 context: Option[LtiDeepLinkingContext])
    extends Lti13Request {
  override val messageType: MessageType = LtiMessageType.LtiDeepLinkingRequest
}

/**
  * Data structure for LTI 1.3 resource link request.
  *
  * todo: Update the structure as needed. Maybe need another case class for the resource link. Check claim "https://purl.imsglobal.org/spec/lti/claim/resource_link".
  */
case class LtiResourceLinkRequest(targetLinkUri: String) extends Lti13Request {
  override val messageType: MessageType = LtiMessageType.LtiResourceLinkRequest
}

object Lti13Request {

  /**
    * Extract custom params from the provided decoded JWT. Custom params are expected to be `String`s,
    * so non-String values can be ignored (discarded).
    *
    * @param decodedJWT Decoded JWT which provides the claim of custom params.
    * @return A key-value map for custom params, or `None` if no such a claim available.
    */
  def getCustomParamsFromClaim(decodedJWT: DecodedJWT): Option[Map[String, String]] = {
    getClaimAsMap(decodedJWT, CUSTOM_PARAMETERS)
      .map(
        _.collect {
          case (k, v) if v.isInstanceOf[String] => (k, v.asInstanceOf[String])
        }
      )
  }

  /**
    * Return the details of a LTI 1.3 request message from the provided decoded token.
    * The request message vary, depending on the message type.
    *
    * @param decodedJWT a token containing claims which provides details of an LTI request message.
    * @return Details of an LTI 1.3 request message, or `Lti13Error` if failed to extract the details.
    */
  def getLtiRequestDetails(decodedJWT: DecodedJWT): Either[OAuth2Error, Lti13Request] = {
    def requiredClaim(claim: String) = getRequiredClaim(decodedJWT, claim)

    def getRequest(messageType: MessageType): Either[InvalidJWT, Lti13Request] = messageType match {
      case LtiMessageType.LtiDeepLinkingRequest =>
        for {
          aud <- decodedJWT.getAudience.asScala.headOption
            .toRight(InvalidJWT(s"Failed to extract audience from JWT"))
          nonce               <- requiredClaim(OpenIDConnectParams.NONCE)
          deploymentId        <- requiredClaim(Lti13Claims.DEPLOYMENT_ID)
          deepLinkingSettings <- LtiDeepLinkingSettings(decodedJWT)
          iss          = decodedJWT.getIssuer
          customParams = getCustomParamsFromClaim(decodedJWT)
          context      = LtiDeepLinkingContext(decodedJWT)
        } yield
          LtiDeepLinkingRequest(iss,
                                aud,
                                nonce,
                                deploymentId,
                                deepLinkingSettings,
                                customParams,
                                context)
      case LtiMessageType.LtiResourceLinkRequest =>
        getRequiredClaim(decodedJWT, Lti13Claims.TARGET_LINK_URI)
          .map(LtiResourceLinkRequest)
    }

    for {
      messageType <- getRequiredClaim(decodedJWT, Lti13Claims.MESSAGE_TYPE)
      validType <- Either
        .catchNonFatal(LtiMessageType.withName(messageType))
        .leftMap(_ => InvalidJWT(s"Unknown LTI message type: $messageType"))
      request <- getRequest(validType)
    } yield request
  }
}
