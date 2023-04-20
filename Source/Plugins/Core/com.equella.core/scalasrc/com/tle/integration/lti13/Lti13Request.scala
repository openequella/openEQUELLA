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

object LtiMessageType extends Enumeration {
  type MessageType = Value
  val LtiDeepLinkingRequest, LtiResourceLinkRequest = Value
}

sealed trait Lti13Request {
  val messageType: MessageType
}

/**
  * Data structure for LTI 1.3 deep linking request.
  *
  * @param deepLinkingSettings Deep Linking settings extracted from claim 'https://purl.imsglobal.org/spec/lti-dl/claim/deep_linking_settings'
  * @param customParams Optional Custom parameters extracted from claim 'https://purl.imsglobal.org/spec/lti/claim/custom'
  */
case class LtiDeepLinkingRequest(deepLinkingSettings: DeepLinkingSettings,
                                 customParams: Option[Map[String, String]])
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
    * Extract custom params from the provided decoded JWT. Values of custom params must be present as String
    * by the LTI platform, so discard those non-String values.
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
  def getLtiRequestDetails(decodedJWT: DecodedJWT): Either[Lti13Error, Lti13Request] = {
    def getRequest(messageType: MessageType): Either[InvalidJWT, Lti13Request] = messageType match {
      case LtiMessageType.LtiDeepLinkingRequest =>
        for {
          deepLinkingSettings <- DeepLinkingSettings(decodedJWT)
          customParams = getCustomParamsFromClaim(decodedJWT)
        } yield LtiDeepLinkingRequest(deepLinkingSettings, customParams)
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
