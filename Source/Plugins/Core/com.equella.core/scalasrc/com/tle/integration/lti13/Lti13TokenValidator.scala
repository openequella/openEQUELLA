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
import com.tle.core.guice.Bind
import com.tle.integration.jwk.JwkProvider
import com.tle.integration.jwt.decodeJwt
import com.tle.integration.oauth2.error.authorisation.InvalidState
import com.tle.integration.oauth2.error.general.InvalidJWT
import com.tle.integration.oidc.verifyIdToken
import javax.inject.{Inject, Singleton}

/**
  * Centralise the functionality of verifying the LTI 1.3 ID token.
  */
@Bind
@Singleton
class Lti13TokenValidator @Inject()(platformService: Lti13PlatformService,
                                    stateService: Lti13StateService,
                                    jwkProvider: JwkProvider,
)(implicit val nonceService: Lti13NonceService) {

  /**
    * Given a previously established `state` with a freshly received ID Token (JWT) will attempt
    * to verify the token inline with the guidance in section 5.1.3 (Authentication Response
    * Validation) of the 1EdTech Security Framework (version 1.1).
    *
    * See: <https://www.imsglobal.org/spec/security/v1p1#authentication-response-validation>
    *
    * The verification includes the following steps:
    *
    * 1. Retrieve the state details associated with the provided state with a basic validation;
    * 2. Decode the raw ID token;
    * 3. Use the state details and decoded token to confirm the platform ID; If the platform ID stored in
    * state is different from the token issuer, stop the process and return an InvalidState error;
    * 4. Retrieve the platform details with the platform ID;
    * 5. Perform the standard OIDC ID token verification with platform details which provides essential
    * information(e.g. expected issuer and client ID) as well as the verified state for nonce verification.
    *
    * @param state the value of the `state` param sent across in an authentication request which
    *              is expected to have been provided from the server in a previous login init
    *              request.
    * @param token an 'ID Token' in JWT format
    * @return Either an error string detailing how things failed, or the actual verified anddecoded JWT ready
    *         for user authentication.
    */
  def verifyToken(state: String, token: String): Either[Lti13Error, DecodedJWT] =
    for {
      stateDetails  <- getStateDetails(state)
      decodedToken  <- decodeToken(token)
      platformId    <- getPlatformId(stateDetails, decodedToken)
      platform      <- platformService.getPlatform(platformId)
      verifiedToken <- verifyToken(decodedToken, platform, state)
    } yield verifiedToken

  private def verifyToken(jwt: DecodedJWT,
                          platform: PlatformDetails,
                          state: String): Either[Lti13Error, DecodedJWT] = {
    val result = for {
      jsonWebKeySetProvider <- jwkProvider.get(platform.keysetUrl)
      jwk = jsonWebKeySetProvider.get(jwt.getKeyId)
      verifiedToken <- verifyIdToken(jwt, platform.platformId, platform.clientId, jwk, state)
    } yield verifiedToken

    result
  }

  // Retrieve the state details with the state sent from LMS.
  private def getStateDetails(s: String): Either[OAuth2LayerError, Lti13StateDetails] =
    stateService.getState(s).toRight(InvalidState(s"Invalid state provided: $s"))

  // Decode the raw ID token. Purpose of this function is for the error type transformation.
  private def decodeToken(t: String): Either[Lti13Error, DecodedJWT] = decodeJwt(t)

  // Get platform ID from the state details and verify it with the issuer in the decoded token.
  private def getPlatformId(stateDetails: Lti13StateDetails,
                            decodedToken: DecodedJWT): Either[Lti13Error, String] =
    Option(stateDetails.platformId)
      .filter(decodedToken.getIssuer.equals)
      .toRight(InvalidJWT(s"Issuer in token did not match stored state."))

}
