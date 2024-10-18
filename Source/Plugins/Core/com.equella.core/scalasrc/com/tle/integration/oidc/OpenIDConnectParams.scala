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

package com.tle.integration.oidc

/**
  * Parameters and claims specific to the OpenID Connect part of OAuth 2.0 exchanges.
  */
object OpenIDConnectParams {

  /**
    * REQUIRED. OAuth 2.0 Client Identifier valid at the Authorization Server.
    */
  val CLIENT_ID = "client_id"

  val EMAIL = "email"

  val FAMILY_NAME = "family_name"

  val GIVEN_NAME = "given_name"

  /**
    * REQUIRED. ID Token.
    */
  val ID_TOKEN = "id_token"

  /**
    * REQUIRED. String value used to associate a Client session with an ID Token, and to mitigate
    * replay attacks. The value is passed through unmodified from the Authentication Request to the
    * ID Token. Sufficient entropy MUST be present in the nonce values used to prevent attackers from
    * guessing values. For implementation notes, see Section 15.5.2.
    */
  val NONCE = "nonce"

  /**
    * REQUIRED. Redirection URI to which the response will be sent. This URI MUST exactly match one
    * of the Redirection URI values for the Client pre-registered at the OpenID Provider, with
    * the matching performed as described in Section 6.2.1 of [RFC3986] (Simple String Comparison).
    * When using this flow, the Redirection URI SHOULD use the https scheme; however, it MAY use the
    * http scheme, provided that the Client Type is confidential, as defined in Section 2.1 of OAuth
    * 2.0, and provided the OP allows the use of http Redirection URIs in this case. The Redirection
    * URI MAY use an alternate scheme, such as one that is intended to identify a callback into a
    * native application.
    */
  val REDIRECT_URI = "redirect_uri"

  val RESPONSE_TYPE = "response_type"

  val RESPONSE_TYPE_ID_TOKEN = "id_token"

  val SCOPE = "scope"

  val SCOPE_OPENID = "openid"

  /**
    * OAuth 2.0 state value. REQUIRED if the `state` parameter is present in the Authorization
    * Request. Clients MUST verify that the `state` value is equal to the value of `state` parameter
    * in the Authorization Request.
    */
  val STATE = "state"
}
