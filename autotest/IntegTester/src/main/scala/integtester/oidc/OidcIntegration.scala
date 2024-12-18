package integtester.oidc

import cats.effect.IO
import cats.implicits._
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import integtester.oidc.OidcUser.TEST_USER
import io.circe.generic.auto._
import io.circe.syntax.EncoderOps
import org.apache.http.client.utils.URIBuilder
import org.http4s.{MediaType, Request, Response, Uri, UrlForm}
import org.http4s.dsl.Http4sDsl
import org.http4s.headers._
import java.security.interfaces.RSAPrivateKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.{KeyFactory, MessageDigest}
import java.time.Instant
import java.util.{Base64, UUID}
import scala.collection.concurrent.TrieMap
import scala.jdk.CollectionConverters._

/** Enum to control what response to return for an auth request
  */
object AuthResponseCommand extends Enumeration {
  val normal, invalid_state, missing_code = Value
}

/** Enum to control what response to return for a token request
  */
object TokenResponseCommand extends Enumeration {
  val invalid_jwt, invalid_nonce, invalid_key, invalid_resp_format, missing_username_claim, normal,
      server_error = Value
}

/** Minimal structure for the response of a successful token request as per the OIDC and OAuth2
  * spec.
  */
final case class OidcTokenResponse(
    id_token: String,
    access_token: String = "",
    token_type: String = "Bearer",
    expires_in: Int = 3600
)

/** Minimal structure for the response of a failed token request as per the OAuth2 spec.
  */
final case class OidcTokenError(
    error: String = "invalid_request",
    error_description: String
)

/** Structure a single user which follows the requirement of Auth0 user structure.
  */
final case class OidcUser(
    user_id: String,
    name: String,
    username: String,
    family_name: String,
    given_name: String,
    email: String
)

object OidcUser {
  val TEST_USER = OidcUser(
    user_id = "1d5214b7-f458-463b-bc62-66fbd45fcf81",
    name = "Test User",
    username = "test_user",
    family_name = "User",
    given_name = "Test",
    email = "test@user"
  )
}

/** Standard OAuth 2 params.
  */
object OidcParams {
  val CLIENT_ID             = "client_id"
  val CLIENT_SECRET         = "client_secret"
  val CODE                  = "code"
  val CODE_CHALLENGE        = "code_challenge"
  val CODE_CHALLENGE_METHOD = "code_challenge_method"
  val CODE_VERIFIER         = "code_verifier"
  val GRANT_TYPE            = "grant_type"
  val NONCE                 = "nonce"
  val REDIRECT_URI          = "redirect_uri"
  val RESPONSE_TYPE         = "response_type"
  val STATE                 = "state"
}

/** Simple Integration Application to assist in testing the OIDC integration with OEQ. This App
  * exposes two commands to control the expected responses for the auth and token requests
  * respectively.
  *
  * Since the test is not interested in how IdPs authenticate theirs users, the external login page
  * is skipped, and this App focuses on other bits, including variety of parameter validations,
  * token exchange and custom claims.
  */
object OidcIntegration extends Http4sDsl[IO] {
  private val REDIRECT_URI           = "http://localhost:8080/vanilla/oidc/callback"
  private val CLIENT_ID              = "test_client"
  private val CLIENT_SECRET          = "your secret"
  private val RESP_TYPE              = "code"
  private val GRANT_TYPE_CREDENTIALS = "client_credentials"
  private val ISSUER                 = "http://localhost:8083/oidc"
  private val KEY_ID                 = "test_key"

  // Command used to control the responses for auth request
  private var authRespCommand: AuthResponseCommand.Value = AuthResponseCommand.normal

  def setAuthRespCommand(c: String): Unit =
    Either.catchNonFatal(AuthResponseCommand.withName(c)) match {
      case Right(v) => authRespCommand = v
      case Left(_)  => authRespCommand = AuthResponseCommand.normal
    }

  // Command used to control the responses for token request
  private var tokenRespCommand: TokenResponseCommand.Value = TokenResponseCommand.normal

  def setTokenRespCommand(c: String): Unit =
    Either.catchNonFatal(TokenResponseCommand.withName(c)) match {
      case Right(v) => tokenRespCommand = v
      case Left(_)  => tokenRespCommand = TokenResponseCommand.normal
    }

  // Storage for the OIDC request details
  private val session: TrieMap[String, Map[String, String]] = new TrieMap()

  /** Validates the login request based on the OAuth2 Authorization Code flow with PKCE. Upon
    * successful validation, redirects the request to the provided callback URL.
    */
  def login(req: Request[IO]): IO[Response[IO]] = {
    def validate: String => Either[String, String] = getRequiredParam(req.params)

    val result: Either[String, Map[String, String]] = for {
      _ <- validate(OidcParams.CLIENT_ID).filterOrElse(_ == CLIENT_ID, "Unknown Client ID")
      _ <- validate(OidcParams.REDIRECT_URI).filterOrElse(_ == REDIRECT_URI, "Unknown redirect URI")
      _ <- validate(OidcParams.RESPONSE_TYPE).filterOrElse(_ == RESP_TYPE, "Invalid response type")
      _ <- validate(OidcParams.STATE).filterOrElse(_.nonEmpty, "Invalid value for state")
      _ <- validate(OidcParams.NONCE).filterOrElse(_.nonEmpty, "Invalid value for nonce")
      _ <- validate(OidcParams.CODE_CHALLENGE)
        .filterOrElse(_.nonEmpty, "Invalid value for code challenge")
      _ <- validate(OidcParams.CODE_CHALLENGE_METHOD).filterOrElse(
        _ == "S256",
        "Invalid value for code challenge method"
      )
    } yield req.params

    result match {
      case Right(details) =>
        val callbackUrl = new URIBuilder(details(OidcParams.REDIRECT_URI))

        val state: String =
          if (authRespCommand == AuthResponseCommand.invalid_state) "invalid state"
          else details(OidcParams.STATE)
        val code: Option[String] =
          if (authRespCommand == AuthResponseCommand.missing_code) None
          else Some(UUID.randomUUID().toString)

        callbackUrl.addParameter(OidcParams.STATE, state)

        for {
          c <- code
        } yield {
          callbackUrl.addParameter(OidcParams.CODE, c)
          session.put(c, details)
        }

        val location = Uri.unsafeFromString(callbackUrl.build().toString)
        Found().map(_.putHeaders(Location(location)))
      case Left(e) => BadRequest(e)
    }
  }

  /** Validates the token request based on the OAuth2 Authorization Code flow with PKCE. Upon
    * successful validation, an ID token (JWT) is generated and returned to the client.
    */
  def token(req: Request[IO]): IO[Response[IO]] =
    req.decode[UrlForm] { form =>
      val data = form.values.view.mapValues(_.toList.head).toMap
      val validate: String => Either[String, String] = getRequiredParam(data)

      def validateClientCredentials = {
        def getCredentials =
          req.headers.get(Authorization).toRight("Missing Authorization header") map { h =>
            val credentials = h.toRaw.value.drop("Basic ".length)
            new String(Base64.getDecoder.decode(credentials)).split(":")
          }

        def getClientId: Either[String, String] = getCredentials.flatMap(cred =>
          cred match {
            case Array(id, _) => Right(id)
            case _            => Left("Failed to retrieve Client ID from the Authorization header")
          }
        )

        def getClientSecret: Either[String, String] = getCredentials.flatMap(cred =>
          cred match {
            case Array(_, secret) => Right(secret)
            case _ => Left("Failed to retrieve Client secret the Authorization header")
          }
        )

        for {
          _ <- getClientId.filterOrElse(_ == CLIENT_ID, "Unknown Client ID")
          _ <- getClientSecret.filterOrElse(_ == CLIENT_SECRET, "Secret does not match Client ID")
        } yield {
          JWT
            .create()
            .withIssuer(ISSUER)
            .withAudience(CLIENT_ID)
            .withIssuedAt(Instant.now)
            .withNotBefore(Instant.now)
            .withExpiresAt(Instant.now.plusSeconds(60))
            .withKeyId(KEY_ID)
            .sign(Algorithm.RSA256(getPrivateKey))
        }
      }

      def validateAuthorisationCode = {
        for {
          _ <- validate(OidcParams.CLIENT_ID).filterOrElse(_ == CLIENT_ID, "Unknown Client ID")
          _ <- validate(OidcParams.CLIENT_SECRET)
            .filterOrElse(_ == CLIENT_SECRET, "Secret does not match Client ID")
          _ <- validate(OidcParams.REDIRECT_URI)
            .filterOrElse(_ == REDIRECT_URI, "Unknown redirect URI")
          authDetails <- validate(OidcParams.CODE).flatMap(session.get(_).toRight("Invalid code"))
          codeChallenge = authDetails(OidcParams.CODE_CHALLENGE)
          _ <- validate(OidcParams.CODE_VERIFIER).filterOrElse(
            verifyCodeChallenge(codeChallenge, _),
            "Code challenge verification fails"
          )
          nonce = authDetails(OidcParams.NONCE)
        } yield {
          val jwt = JWT
            .create()
            .withIssuer(
              if (tokenRespCommand == TokenResponseCommand.invalid_jwt) "bad issuer" else ISSUER
            )
            .withAudience(CLIENT_ID)
            .withIssuedAt(Instant.now)
            .withNotBefore(Instant.now)
            .withExpiresAt(Instant.now.plusSeconds(60))
            .withKeyId(
              if (tokenRespCommand == TokenResponseCommand.invalid_key) "bad key ID" else KEY_ID
            )
            .withClaim(
              "nonce",
              if (tokenRespCommand == TokenResponseCommand.invalid_nonce) "bad nonce" else nonce
            )
            .withSubject(TEST_USER.user_id)
            .withClaim("family_name", TEST_USER.family_name)
            .withClaim("given_name", TEST_USER.given_name)
            .withClaim("email", TEST_USER.email)
            .withClaim("roles", List("developer").asJava)

          if (tokenRespCommand != TokenResponseCommand.missing_username_claim) {
            jwt.withClaim("username", TEST_USER.username)
          }

          jwt.sign(Algorithm.RSA256(getPrivateKey))
        }
      }

      val result = for {
        grantType <- validate(OidcParams.GRANT_TYPE)
        jwt <-
          if (grantType == GRANT_TYPE_CREDENTIALS) validateClientCredentials
          else validateAuthorisationCode
      } yield jwt

      result match {
        case Right(jwt) =>
          val resp = if (tokenRespCommand != TokenResponseCommand.invalid_resp_format) {
            OidcTokenResponse(id_token = jwt).asJson.noSpaces
          } else s"{\"id_token\":\"$jwt\"}"

          tokenRespCommand match {
            case TokenResponseCommand.server_error => InternalServerError("Auth server error")
            case _ => Ok(resp, `Content-Type`(MediaType.application.json))
          }
        case Left(e) =>
          val resp = OidcTokenError(error_description = e).asJson.noSpaces
          BadRequest(resp, `Content-Type`(MediaType.application.json))
      }
    }

  def jwks: IO[Response[IO]] = {
    val publicKey =
      s"""
         |{
         |  "keys": [
         |    {
         |      "kty" : "RSA",
         |      "e" : "AQAB",
         |      "use" : "sig",
         |      "kid" : "$KEY_ID",
         |      "alg" : "RS256",
         |      "n" : "i48HmBJLRI-2aIPdwzzcgjdCOmJXl_tcG1WESgLm4_XtKzdut0xc23A7NgDWn2oDPfTfZHmGFu1j71LWxyCCSRPcCTmIuF1cb52XYPMWWcpLapjzqnLhzm4VvtQjmc-XzP8wVUNi44dbO5gx8xWGbGthH3Xg3m9Bc4_Fb4k60Sr4dvAT3pHoXcF5YbODTRO1CmpcvifAkq0jSt_vOsvuDD4b-UFk1hHcnLjh6As4ISfxgH8AvCkQTyiSalM38hA-En4WWFxEUK_SSYeONSCvB8ws3LMHjSjnwoevp2VQXaYhnC30fiAJw6Xe0E7d1F_ft9NFUzy8OuENnfXgcXD1zQ=="
         |    }
         |  ]
         |}
         |""".stripMargin
    Ok(publicKey, `Content-Type`(MediaType.application.json))
  }

  def users: IO[Response[IO]] = {
    val NEW_USER = TEST_USER.copy(
      user_id = UUID.randomUUID().toString,
      username = "test_user_b",
      email = "test.b@user"
    )

    val resp = List(TEST_USER, NEW_USER).asJson.noSpaces
    Ok(resp, `Content-Type`(MediaType.application.json))
  }

  def user: IO[Response[IO]] = {
    Ok(TEST_USER.asJson.noSpaces, `Content-Type`(MediaType.application.json))
  }

  private def getRequiredParam(params: Map[String, String]): String => Either[String, String] =
    key => params.get(key).toRight(s"Missing required parameter: $key")

  private def getPrivateKey = {
    val key =
      "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCLjweYEktEj7Zog93DPNyCN0I6YleX+1wbVYRKAubj9e0rN263TFzbcDs2ANafagM99N9keYYW7WPvUtbHIIJJE9wJOYi4XVxvnZdg8xZZyktqmPOqcuHObhW+1COZz5fM/zBVQ2Ljh1s7mDHzFYZsa2EfdeDeb0Fzj8VviTrRKvh28BPekehdwXlhs4NNE7UKaly+J8CSrSNK3+86y+4MPhv5QWTWEdycuOHoCzghJ/GAfwC8KRBPKJJqUzfyED4SfhZYXERQr9JJh441IK8HzCzcsweNKOfCh6+nZVBdpiGcLfR+IAnDpd7QTt3UX9+300VTPLw64Q2d9eBxcPXNAgMBAAECggEAAf/Ht6soOBBojoRR3IaDEIjcgmjL1G0dwdtIJj9DmEEuAPZ6t+/e5kEFuB2FW/DXoKWUJaS2aDgSVgIbxx9CGB9+tJ5SxQ4hUT//d4lD6Rld8//CgQIp4rXul2mc8PEcvZHSQWzSF9Kyjd7KQfv6JvqkZrnFKBDz7d6oKpOoRTxduiI7hSl5VDdcYZSgJIw6vK7m1HmvH1iA5PY8XovcM7G+NP1aNlNACSKFyhaYgKOv4kbPOOOCFvjv49LGnI12ZQjHMx65/gNI68J1etsyW+MgVD+OsLQG3ZZ88ygDgKX0f7jiLjTWl2cYKZj+vwWwSRkBrNC8+JDPC5nt/JX3iQKBgQDDWPcT7x9+o9yV96kACABAB3mtPPFxa0w+bMLcx6p3olwWwlt3Xm2YNiDthQIBXO2UcsUvyl/hpzzJDTjB3keJRrLwIvnkqaozBWVVKVvOpI+2QOSc7KYayyqL03Frlw5FhAOTD8OGi5YkzYtVtYMucFukWgzIelWNux3HEea99wKBgQC247zGgjWiF/J4C6Jr021P5SbrcqyMLGOOMHwKffnF8SbDyZ2cL1y5cVlPvAurr4+geN3ynzwZXUBpf0fTvPCWclvwBpxs+eX0K1MT7c6UgLfExKGoZtUpPXUZWDdzKCU8Ywz07ls0SrCfURdtEKw7zwXe3iIMe+MxdUji6cBJWwKBgFoWGgOQX/bX/G8QglhoWC0vBVb15uaGM+gJ4idM0PlQ36UDRoka+/GY7MB8eTtFrq6p6vOqIWN/61FQjp8hMd5Fw1lo19MyxzG2SE/ovdTjIP9Ml+EiZUuv69QF06VpSQSX58oXF/VhRAF+5MXG6lMtL6XwfTqq5+9vnkDiqYVrAoGAOlAiwKiDsUsxZFVRVE/1aqQF5MyLd6YvmAadhjrkiNh7I7IFLudrCTW65FFw5yzy/peLLPDYmL3C6+BJZpm3nECaT83L0nNbfp7Rvmj0lST1cye+45LmJGAUFiCqjW0fckGiv/W/3sxxxFp2va+zOt6nYIgzEwftSLe8pZ0iUkECgYEAtHeZQdhnxmTEx4OBoSHx90U/3eQLRClCMP/OOcK6kvn50pmA4n4sOJklf1IRUvKEElz3zAVfazzUEre9OpzgZjJhcsfRKwne3JawUe37sOo1+ffyhqPKLDzfGdnkLwInKvCBPY4Qvr/FgxKyQR1ULMI8kZz6h+Xbq+IjlbMY9Ak="
    val factory = KeyFactory.getInstance("RSA")
    val encoded = Base64.getDecoder.decode(key)
    factory.generatePrivate(new PKCS8EncodedKeySpec(encoded)).asInstanceOf[RSAPrivateKey]
  }

  private def verifyCodeChallenge(challenge: String, verifier: String): Boolean = {
    val digest = MessageDigest.getInstance("SHA-256").digest(verifier.getBytes("US-ASCII"))
    val expectedChallenge = Base64.getUrlEncoder.withoutPadding.encodeToString(digest)
    challenge == expectedChallenge
  }
}
