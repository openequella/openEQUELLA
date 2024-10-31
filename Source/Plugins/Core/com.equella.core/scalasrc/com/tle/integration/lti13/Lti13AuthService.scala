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

import cats.implicits._
import com.auth0.jwt.interfaces.DecodedJWT
import com.google.common.hash.HashCode
import com.google.common.hash.Hashing.murmur3_128
import com.tle.beans.user.TLEUser
import com.tle.common.institution.CurrentInstitution
import com.tle.common.usermanagement.user.{UserState, WebAuthenticationDetails}
import com.tle.common.util.StringUtils.generateRandomHexString
import com.tle.core.guice.Bind
import com.tle.core.institution.RunAsInstitution
import com.tle.core.lti13.service.LtiPlatformService
import com.tle.core.security.impl.AclExpressionEvaluator
import com.tle.core.services.user.UserService
import com.tle.core.usermanagement.standard.service.{TLEGroupService, TLEUserService}
import com.tle.exceptions.UsernameNotFoundException
import com.tle.integration.jwk.JwkProvider
import com.tle.integration.jwt.decodeJwt
import com.tle.integration.lti13.OAuth2LayerError._
import com.tle.integration.lti13.{Lti13Params => LTI13}
import com.tle.integration.oauth2._
import com.tle.integration.oidc.{getClaim, verifyIdToken, OpenIDConnectParams => OIDC}
import com.tle.integration.util.{getParam, getUriParam}
import io.circe._
import io.circe.parser._
import io.lemonlabs.uri.{QueryString, Url}
import org.slf4j.LoggerFactory

import java.net.URI
import java.nio.charset.StandardCharsets
import java.util.Base64
import javax.inject.{Inject, Singleton}
import scala.annotation.tailrec
import scala.jdk.CollectionConverters._
import scala.util.{Failure, Success, Try}

/**
  * Captures the parameters which make up the first part of a 'Third-party Initiated Login' as part
  * of an 'OpenID Connect Launch Flow' which is detailed in section 5.1.1 of version 1.1 of the
  * 1EdTech Security Framework. The first three values (`iss`, `login_hint` and `target_link_uri`)
  * are specified in the Security Framework, where as the remaining additional parameters are part
  * of the LTI 1.3 specification - section 4.1 Additional login parameters.
  *
  * @param iss REQUIRED. The issuer identifier identifying the learning platform.
  * @param login_hint REQUIRED. Hint to the Authorization Server about the login identifier the
  *                   End-User might use to log in. The permitted values will be defined in the
  *                   host specification.
  * @param target_link_uri REQUIRED. The actual end-point that should be executed at the end of the
  *                        OpenID Connect authentication flow.
  * @param lti_message_hint The new optional parameter lti_message_hint may be used alongside the
  *                         login_hint to carry information about the actual LTI message that is
  *                         being launched.
  * @param lti_deployment_id The new optional parameter lti_deployment_id that if included, MUST
  *                          contain the same deployment id that would be passed in the
  *                          https://purl.imsglobal.org/spec/lti/claim/deployment_id claim for the
  *                          subsequent LTI message launch.
  * @param client_id The new optional parameter client_id specifies the client id for the
  *                  authorization server that should be used to authorize the subsequent LTI message
  *                  request. This allows for a platform to support multiple registrations from a
  *                  single issuer, without relying on the initiate_login_uri as a key.
  */
case class InitiateLoginRequest(iss: String,
                                login_hint: String,
                                target_link_uri: URI,
                                lti_message_hint: Option[String],
                                lti_deployment_id: Option[String],
                                client_id: Option[String])
object InitiateLoginRequest {
  def apply(params: Map[String, Array[String]]): Option[InitiateLoginRequest] = {
    val param    = getParam(params)
    val uriParam = getUriParam(param)

    for {
      iss             <- param(Lti13Params.ISSUER)
      login_hint      <- param(Lti13Params.LOGIN_HINT)
      target_link_uri <- uriParam(Lti13Params.TARGET_LINK_URI)
      lti_message_hint  = param(Lti13Params.LTI_MESSAGE_HINT)
      lti_deployment_id = param(Lti13Params.LTI_DEPLOYMENT_ID)
      client_id         = param(Lti13Params.CLIENT_ID)
    } yield
      InitiateLoginRequest(iss,
                           login_hint,
                           target_link_uri,
                           lti_message_hint,
                           lti_deployment_id,
                           client_id)
  }
}

/**
  * Captures the parameters which make up the third step of a 'Third-party Initiated Login' as part
  * of an 'OpenID Connect Launch Flow' which is detailed in section 5.1.1 of version 1.1 of the
  * 1EdTech Security Framework. These values are ultimately the authentication details to be
  * validated by openEQUELLA to establish a user session.
  *
  * These parameters are the standard ones outlined in section 3.2.2.5 (Successful Authentication
  * Response) of the OpenID Connect Core 1.0 specification.
  *
  * @param state OAuth 2.0 state value. REQUIRED if the state parameter is present in the
  *              Authorization Request. Clients MUST verify that the state value is equal to the
  *              value of state parameter in the Authorization Request.
  * @param id_token REQUIRED. ID Token. (As in OAuth 2.0 ID Token.)
  */
case class AuthenticationResponse(state: String, id_token: String)
object AuthenticationResponse {
  def apply(params: Map[String, Array[String]]): Option[AuthenticationResponse] = {
    val param = getParam(params)

    for {
      state    <- param(OIDC.STATE)
      id_token <- param(OIDC.ID_TOKEN)
    } yield AuthenticationResponse(state, id_token)
  }
}

/**
  * Represents an LTI User's details.
  *
  * @param platformId the ID of the LTI Platform from which the user is from
  * @param userId a stable locally unique (to the platform) identifier - typically supplied in a
  *               `sub` claim
  * @param roles supplied in the 'roles claim' which is defined as: The required https://purl.imsglobal.org/spec/lti/claim/roles
  *              claim's value contains a (possibly empty) array of URI values for roles that the
  *              user has within the message's associated context.
  *
  *              If this list is not empty, it MUST contain at least one role from the role
  *              vocabularies described in role vocabularies.
  * @param firstName the users first name (OIDC 'given_name') - TLEUser requires one of these, so we
  *                   make it mandatory
  * @param lastName the users last name (OIDC 'family_name') - TLEUser requires one of these, so we
  *                  make it mandatory
  * @param email the users email address (OIDC 'email') - TLEUser allows this to be null, so hence we
  *              treat it as optional with Option.
  */
case class UserDetails(platformId: String,
                       userId: String,
                       roles: List[String],
                       firstName: String,
                       lastName: String,
                       email: Option[String])
object UserDetails {
  def apply(jwt: DecodedJWT,
            usernameClaimPaths: Option[List[String]]): Either[OAuth2LayerError, UserDetails] = {
    val claim = getClaim(jwt)

    // Use the custom username claim if present, otherwise use the Subject claim.
    // The payload is in JSON format so use Circe to parse and traverse the JSON string.
    // The claim has been verified earlier, so if parsing the token or reading the value
    // fails, it's mostly because the token is invalid.
    def getUserId: Either[InvalidJWT, String] = {
      def fromCustomClaim(paths: List[String]): Either[InvalidJWT, String] = {
        val jwtPayload =
          new String(Base64.getUrlDecoder.decode(jwt.getPayload), StandardCharsets.UTF_8)

        // The claim is verified so we can simply build the original string in the required format.
        def originalClaim = paths.map(c => s"[$c]").mkString

        def read(doc: Json): Decoder.Result[String] =
          paths
            .foldLeft[ACursor](doc.hcursor) { (cursor, key) =>
              cursor.downField(key)
            }
            .as[String]

        parse(jwtPayload)
          .flatMap(read)
          .leftMap(_ => InvalidJWT(s"Unable to determine user ID by claim $originalClaim"))
      }

      def fromSubjectClaim: Either[InvalidJWT, String] =
        Option(jwt.getSubject)
          .toRight(InvalidJWT("No subject claim provided in JWT, unable to determine user id."))

      usernameClaimPaths.map(fromCustomClaim).getOrElse(fromSubjectClaim)
    }

    val userDetails = for {
      platformId <- Option(jwt.getIssuer)
        .toRight(
          InvalidJWT(
            "No issuer claim provided in the JWT, unable to determine origin LTI platform."))
      userId <- getUserId
      emptyRoles = List() // helper for readability
      // Attempt to get the claim with the roles. However it could be absent or empty
      // in which case we just go with an 'empty' list of roles. (There is also the case where
      // perhaps the claim is in a format other than we expect, so catch that error situation.)
      roles <- Option(jwt.getClaim(Lti13Claims.ROLES))
        .filterNot(_.isNull)
        .map(c =>
          Try {
            Option(c.asList(classOf[String])).map(_.asScala.toList)
          } match {
            case Failure(_)     => Left(InvalidJWT("Provided roles claim is not a valid format."))
            case Success(value) => Right(value.getOrElse(emptyRoles))
        })
        .getOrElse(Right(emptyRoles))

      // Get the additional identify information - used to later setup a TLEUser. The LMS may need
      // to have configuration set to include these in the result. (For example, in Moodle you
      // need to go into the 'Privacy' settings for the External Tool.)
      firstName <- claim(OIDC.GIVEN_NAME)
        .toRight(InvalidJWT(s"No first name (${OIDC.GIVEN_NAME}) was provided."))
      lastName <- claim(OIDC.FAMILY_NAME)
        .toRight(InvalidJWT(s"No last name (${OIDC.FAMILY_NAME}) was provided."))
      email = claim(OIDC.EMAIL)
    } yield UserDetails(platformId, userId, roles, firstName, lastName, email)

    userDetails
  }
}

/**
  * Responsible for the authentication of LTI requests, and where necessary establishing an
  * authenticated openEQUELLA user session (with `UserState`) for an LTI user.
  */
@Bind
@Singleton
class Lti13AuthService {
  private val LOGGER = LoggerFactory.getLogger(classOf[Lti13AuthService])

  @Inject private implicit var nonceService: Lti13NonceService = _
  @Inject private var platformService: Lti13PlatformService    = _
  @Inject private var runAs: RunAsInstitution                  = _
  @Inject private var stateService: Lti13StateService          = _
  @Inject private var tleGroupService: TLEGroupService         = _
  @Inject private var tleUserService: TLEUserService           = _
  @Inject private var userService: UserService                 = _
  @Inject private var jwkProvider: JwkProvider                 = _
  @Inject private var tokenValidator: Lti13TokenValidator      = _

  /**
    * In response to a Third-Party Initiated Login, create the resulting URL which the UA should be
    * redirected so that the Authentication process can begin.
    *
    * @return a URL containing the query params as per section 5.1.1.2 of the LTI 1.3 spec
    */
  def buildAuthReqUrl(initReq: InitiateLoginRequest): Option[String] = {
    for {
      platformDetails  <- platformService.getPlatform(initReq.iss).toOption
      authUrl          <- Url.parseOption(platformDetails.authUrl.toString)
      lti_message_hint <- initReq.lti_message_hint
      state = stateService.createState(
        Lti13StateDetails(initReq.iss, initReq.login_hint, initReq.target_link_uri))
    } yield
      authUrl
        .withQueryString(
          QueryString.fromPairs(
            OIDC.SCOPE             -> OIDC.SCOPE_OPENID,
            OIDC.RESPONSE_TYPE     -> OIDC.RESPONSE_TYPE_ID_TOKEN,
            OIDC.CLIENT_ID         -> platformDetails.clientId,
            OIDC.REDIRECT_URI      -> getRedirectUri.toString,
            LTI13.LOGIN_HINT       -> initReq.login_hint,
            OIDC.STATE             -> state,
            LTI13.RESPONSE_MODE    -> LTI13.RESPONSE_MODE_FORM_POST,
            LTI13.NONCE            -> nonceService.createNonce(state),
            LTI13.PROMPT           -> LTI13.PROMPT_NONE,
            LTI13.LTI_MESSAGE_HINT -> lti_message_hint
          ))
        .toString()
  }

  /**
    * Given a previously established `state` with a freshly received ID Token (JWT) will attempt
    * to verify the token inline with the guidance in section 5.1.3 (Authentication Response
    * Validation) of the 1EdTech Security Framework (version 1.1).
    *
    * See: <https://www.imsglobal.org/spec/security/v1p1#authentication-response-validation>
    *
    * @param state the value of the `state` param sent across in an authentication request which
    *              is expected to have been provided from the server in a previous login init
    *              request.
    * @param token an 'ID Token' in JWT format
    * @return Either a error string detailing how things failed, or the actual decoded JWT and details of the LTI platform.
    */
  def verifyToken(state: String, token: String): Either[Lti13Error, DecodedJWT] =
    tokenValidator.verifyToken(state, token)

  /**
    * Use the provided ID token to build a `UserDetails` for a user authenticating via LTI, and then attempt
    * to establish a session for the user. The result (on success) will be a new `UserState` instance that
    * will be stored against the user's session.
    *
    * @param wad the details of the HTTP request for this authentication attempt
    * @param token Decoded ID token that has been verified
    * @return a new `UserState` being used for the new session OR a string representing what failed.
    */
  def loginUser(wad: WebAuthenticationDetails, token: DecodedJWT): Either[Lti13Error, UserState] = {

    val loginResult = for {
      // Get the platform details, verify the custom username claim and then build UserDetails
      platformDetails    <- platformService.getPlatform(token.getIssuer)
      usernameClaimPaths <- platformService.verifyUsernameClaim(platformDetails)
      userDetails        <- UserDetails(token, usernameClaimPaths)
      // Setup the user - including adding roles
      userState <- mapUser(
        user = userDetails,
        platform = platformDetails,
        authenticate = username => Try(userService.authenticateAsUser(username, wad)),
        asGuest = () => userService.authenticateAsGuest(wad)
      ).left.map(error => {
        LOGGER.error(
          s"Failed to authenticate user ${userDetails.userId} from platform ${userDetails.platformId}: ${error}")
        error
      })
      _ = addRolesToUser(userState, userDetails, platformDetails)

      // And check against any ACLExpression
      allowedUserState <- platformDetails.allowExpression match {
        case Some(expression) =>
          val aclExpressionEvaluator = new AclExpressionEvaluator
          Either.cond[OAuth2LayerError, UserState](
            aclExpressionEvaluator.evaluate(expression, userState, false),
            userState,
            NotAuthorized(
              s"User ${userDetails.userId} from platform ${userDetails.platformId} is currently not permitted access: ACL Expression violation")
          )
        case None => Right(userState)
      }
    } yield {
      LOGGER.debug(s"loginUser($userDetails)")
      userService.login(allowedUserState, true)
      allowedUserState
    }

    loginResult
  }

  def getRedirectUri: URI =
    new URI(s"${CurrentInstitution.get().getUrl}lti13/launch")

  /**
    * Given `UserDetails` from an LTI (OAuth2) ID Token, and the configuration details for the
    * platform (`PlatformDetails`) attempt to 'map' the LTI User to an openEQUELLA `UserState`.
    * Depending on the platform configuration for unknown user handling, this could be a 'Guest'
    * `UserState`, an existing user's `UserState`, or a newly created user's `UserState`. Any errors
    * during this process will be returned as a `Left[String]`, including if the platform's
    * configuration for 'unknown user handling' is to return an authentication error.
    *
    * @param user the details of a user from an 'ID token' to be mapped
    * @param platform the configuration details of the platform from which the user is
    *                 authenticating
    * @param authenticate a function which given an openEQUELLA username, will return a matching
    *                     `UserState`
    * @param asGuest a function which can produce a `UserState` representing a guest user
    * @return Will return a `UserState` representing the provided `user` adhering to the
    *         configuration of the `platform`. Or on error, will return a `Left[String]` of what the
    *         issue was.
    */
  private def mapUser(user: UserDetails,
                      platform: PlatformDetails,
                      authenticate: String => Try[UserState],
                      asGuest: () => UserState): Either[OAuth2LayerError, UserState] = {
    val ltiUserId = user.userId
    // The username which will be seen and used in the system
    val username = platform.usernamePrefix.getOrElse("") + ltiUserId + platform.usernameSuffix
      .getOrElse("")

    def handleUnknownUser(): Either[OAuth2Error, UserState] = {
      // A unique ID for the user in the oEQ DB - not used elsewhere for authentication, but we
      // need to meeting existing requirements of the tle_user table.
      val oeqUserId                                       = genId(platform.platformId, ltiUserId)
      val (unknownUserHandling, unknownUserDefaultGroups) = platform.unknownUserHandling

      unknownUserHandling match {
        case UnknownUserHandling.ERROR =>
          Left(AccessDenied(s"Failed to authenticate (Unknown User)"))
        case UnknownUserHandling.GUEST => Right(asGuest())
        case UnknownUserHandling.CREATE =>
          LOGGER.info(s"Creating new user $username($oeqUserId).")
          // - Create a TLEUser
          val newUser = new TLEUser()
          newUser.setUuid(oeqUserId)
          newUser.setUsername(username)
          newUser.setFirstName(user.firstName)
          newUser.setLastName(user.lastName)
          user.email.foreach(newUser.setEmailAddress)
          newUser.setPassword(generateRandomHexString(20))
          // - Add the TLEUser, and set them up with any configured groups
          runAs.executeAsSystem(
            CurrentInstitution.get(),
            () => {
              // TODO: Could use some error handling - although, the old LTI code didn't for some reason
              LOGGER.debug(s"tleUserService.add(${newUser.getUsername})")
              tleUserService.add(newUser)
              unknownUserDefaultGroups.map(_.foreach(groupId => {
                LOGGER.debug(s"tleGroupService.addUserToGroup($groupId, ${newUser.getUuid})")
                tleGroupService.addUserToGroup(groupId, newUser.getUuid)
              }))
            }
          )

          // We have a new user, so establish UserState for them
          authenticate(username).toEither.left.map(t => {
            LOGGER.error(
              s"Failed to authenticate with newly created LTI 1.3 user - $username($oeqUserId): ${t.getMessage}")
            ServerError(s"Failed to authenticate as newly created user: $username")
          })
      }
    }

    authenticate(username) match {
      case Failure(_: UsernameNotFoundException) => handleUnknownUser()
      case Failure(exception)                    => Left(ServerError(exception.getMessage))
      case Success(userState)                    => Right(userState)
    }
  }

  /**
    * Inspects the roles in the provided `UserDetails` for the standard LIS (v2) context role
    * identifying the user as an 'Instructor'. Does _not_ support 'simple names' as this method
    * is considered deprecated and so "by best practice, vendors should use the full URIs for all
    * roles (context roles included)".
    *
    * @param userDetails the user to check for the instructor role
    * @return `true` if the target user is an instructor
    */
  private def isInstructor(userDetails: UserDetails): Boolean =
    userDetails.roles.exists(Lti13Claims.instructorRolePredicate)

  /**
    * Based on the details of the user (`userDetails`) add the configuration for the `platform`, add
    * the required roles to the provided `userState` - i.e. mutate it in place.
    *
    * @param userState the target UserState to have the roles added to
    * @param userDetails the details of the user to determine which roles should be added
    * @param platform configuration of the platform which includes the role configuration
    */
  private def addRolesToUser(userState: UserState,
                             userDetails: UserDetails,
                             platform: PlatformDetails): Unit = {
    // 1. Determine instructor roles - if instructor
    val instructorRoles = if (isInstructor(userDetails)) platform.instructorRoles else Set.empty

    // 2. Determine custom roles - if matching roles are on user
    val customRoles = platform.customRoles
      .filter({
        case (role, _) => userDetails.roles.contains(role)
      })
      .flatMap {
        case (_, oeqRoles) => oeqRoles
      }
      .toSet

    // 3. Determine if additional roles are needed - if any were unknown
    val hasUnknownRoles = userDetails.roles
      .filterNot(Lti13Claims.instructorRolePredicate)
      .filterNot(platform.customRoles.contains)
      .nonEmpty
    val additionalRoles = if (hasUnknownRoles) platform.unknownRoles else Set.empty

    // Finally - update the UserState with all the values
    // This is horrible (using a getter to access an internal object to mutate) - however this
    // is the official way it's done with oEQ UserState management.
    userState.getUsersRoles.addAll(
      (instructorRoles ++ customRoles ++ additionalRoles).asJavaCollection)
  }

  /**
    * Generates a unique structured identifier for the provided user from the specified platform.
    * The identifier has the structure of `LTI13:<platformid>_<userid>` which is further explained
    * as:
    *
    * - `platformid` is the first 10 characters of a base64 representation of a hash, with
    * the intention that this should be unique enough to identify the platform from other
    * platforms - similar to how short hashes are used in git.
    *  - `userid` is a 128bit hash of `userId` which is represented in base 64 format.
    *
    * NOTE: Above where base64 is referred to this is in a numerical encoding sense, not a byte
    * encoding sense as often used on the internet.
    *
    * @param platformId the id of the platform within which `userId` is relevant
    * @param userId     a user id specific to `platformId`
    * @return an (ideally) unique structured ID that will fit within 40 characters.
    */
  private def genId(platformId: String, userId: String): String = {

    /**
      * Given a hash (which after all is a number represented by an array of bytes), generate a base
      * 64 representation of that number. This implementation is not planned to be a generic base 64
      * representation method, and so has not been properly validated - only validated within this
      * context.
      *
      * @param hashCode the hash to be represented in base 64
      * @return `hashCode` as a base 64 number
      */
    def base64HashCode(hashCode: HashCode): String = {
      val base   = 64
      val lookup = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz+/"
      // The hashcode byte array is prefixed with a zero byte to force it positive
      val bigIntHashCode = new BigInt(
        new java.math.BigInteger(Array[Byte](0x00) ++ hashCode.asBytes()))

      @tailrec
      def enc(x: BigInt, result: String): String = {
        if (x > 0) enc(x / base, s"${lookup.charAt(x.mod(base).intValue)}" + result)
        else result
      }

      enc(bigIntHashCode, "")
    }

    // Only a hash for uniqueness is required, not for security.
    // So we go with the NCH algorithm murmur3.
    def base64Murmur3(input: String): String = {
      val hash = murmur3_128().hashBytes(input.getBytes)
      base64HashCode(hash)
    }

    val pid = base64Murmur3(platformId)
    val uid = base64Murmur3(userId)

    // To keep things short, we only use the first 10 bytes for the platform id - that should be
    // unique enough.
    s"LTI13:${pid.substring(0, 10)}_$uid"
  }
}
