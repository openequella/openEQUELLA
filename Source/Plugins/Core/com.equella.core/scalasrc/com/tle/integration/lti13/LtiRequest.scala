package com.tle.integration.lti13

object LtiMessageType extends Enumeration {
  val LtiDeepLinkingRequest, LtiResourceLinkRequest = Value
}

trait LtiRequest {
  val messageType: LtiMessageType.Value
}

/**
  * Data structure for LTI 1.3 deep linking request.
  *
  * @param deepLinkingSettings Deep Linking settings extracted from claim 'https://purl.imsglobal.org/spec/lti-dl/claim/deep_linking_settings'
  * @param customParams Optional Custom parameters extracted from claim 'https://purl.imsglobal.org/spec/lti/claim/custom'
  */
case class LtiDeepLinkingRequest(deepLinkingSettings: DeepLinkingSettings,
                                 customParams: Option[Map[String, String]])
    extends LtiRequest {
  override val messageType: LtiMessageType.Value = LtiMessageType.LtiDeepLinkingRequest
}

/**
  * Data structure for LTI 1.3 resource link request.
  *
  * todo: Update the structure as needed. Maybe need another case class for the resource link. Check claim "https://purl.imsglobal.org/spec/lti/claim/resource_link".
  */
case class LtiResourceLinkRequest(targetLinkUri: String) extends LtiRequest {
  override val messageType: LtiMessageType.Value = LtiMessageType.LtiResourceLinkRequest
}
