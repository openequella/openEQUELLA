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

/** HTTP Parameters for the various LTI HTTP requests.
  */
object Lti13Params {

  /** The new optional parameter `client_id` specifies the client id for the authorization server
    * that should be used to authorize the subsequent LTI message request. This allows for a
    * platform to support multiple registrations from a single issuer, without relying on the
    * `initiate_login_uri` as a key.
    */
  val CLIENT_ID = "client_id"

  /** The issuer identifier identifying the learning platform.
    */
  val ISSUER = "iss"

  /** Hint to the Authorization Server about the login identifier the End-User might use to log in.
    * The permitted values will be defined in the host specification.
    */
  val LOGIN_HINT = "login_hint"

  /** The new optional parameter `lti_deployment_id` that if included, MUST contain the same
    * deployment id that would be passed in the
    * <https://purl.imsglobal.org/spec/lti/claim/deployment_id> claim for the subsequent LTI message
    * launch.
    *
    * This parameter may be used by the tool to perform actions that are dependent on a specific
    * deployment. An example of this would be, using the deployment id to identify the region in
    * which a tenant linked to the deployment lives. Subsequently changing the `redirect_url` the
    * final launch will be directed to.
    */
  val LTI_DEPLOYMENT_ID = "lti_deployment_id"

  /** The new optional parameter `lti_message_hint` may be used alongside the `login_hint` to carry
    * information about the actual LTI message that is being launched.
    *
    * Similarly to the `login_hint` parameter, `lti_message_hint` value is opaque to the tool. If
    * present in the login initiation request, the tool MUST include it back in the authentication
    * request unaltered.
    */
  val LTI_MESSAGE_HINT = "lti_message_hint"

  /** REQUIRED. String value used to associate a Client session with an ID Token, and to mitigate
    * replay attacks. The value is passed through unmodified from the Authentication Request to the
    * ID Token.
    */
  val NONCE = "nonce"

  /** REQUIRED. Since the message launch is meant to be sent from a platform where the user is
    * already logged in. If the user has no session, a platform must just fail the flow rather than
    * ask the user to log in.
    */
  val PROMPT      = "prompt"
  val PROMPT_NONE = "none"

  /** REQUIRED. The Token can be lengthy and thus should be passed over as a form POST.
    */
  val RESPONSE_MODE           = "response_mode"
  val RESPONSE_MODE_FORM_POST = "form_post"

  /** The actual end-point that should be executed at the end of the OpenID Connect authentication
    * flow.
    */
  val TARGET_LINK_URI = "target_link_uri"
}
