package integtester.testprovider

import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

import cats.data.{Kleisli, OptionT}
import cats.effect.{ContextShift, IO}
import cats.syntax.semigroupk._
import integtester.IntegTester
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.AuthMiddleware
import scalaoauth2.provider._

import scala.collection.JavaConverters._
import scala.concurrent.{ExecutionContext, Future}

case class TestUser(clientId: String)

class TestingCloudProvider(implicit val cs: ContextShift[IO]) extends Http4sDsl[IO] {

  case class OAuthTokenResponse(access_token: String,
                                refresh_token: Option[String],
                                token_type: Option[String],
                                expires_in: Option[Long],
                                state: Option[String])

  import io.circe.generic.auto._

  import scala.concurrent.ExecutionContext.Implicits.global

  val sampleConfig = Iterable(
    RCloudControlConfig("something",
                        "Something Field",
                        None,
                        RCloudControlConfigType.Textfield,
                        Iterable.empty,
                        1,
                        1),
    RCloudControlConfig("somethingElse",
                        "Where do you want to store it?",
                        None,
                        RCloudControlConfigType.XPath,
                        Iterable.empty,
                        1,
                        1),
    RCloudControlConfig(
      "colour",
      "Which colour do you want?",
      Some("Please choose a colour"),
      RCloudControlConfigType.Dropdown,
      Iterable(RCloudConfigOption("Yellow", "y"), RCloudConfigOption("Green", "g")),
      1,
      1
    ),
    RCloudControlConfig(
      "colours",
      "Which other colours do you want?",
      Some("Please choose some colour(s)"),
      RCloudControlConfigType.Check,
      Iterable(RCloudConfigOption("Yellow", "y"), RCloudConfigOption("Green", "g")),
      0,
      1
    ),
    RCloudControlConfig(
      "radio",
      "Which station do you listen to?",
      Some("Please choose a station"),
      RCloudControlConfigType.Radio,
      Iterable(RCloudConfigOption("Tripl J", "92.9"), RCloudConfigOption("Sea FM", "who cares")),
      0,
      1
    )
  )

  def headerMap(request: Request[IO]): Map[String, Seq[String]] =
    request.headers.groupBy(_.name.value).mapValues(_.map(_.value).toSeq)

  val authUser: Kleisli[OptionT[IO, ?], Request[IO], TestUser] =
    Kleisli { request =>
      val resourceReq = new ProtectedResourceRequest(headerMap(request), Map.empty)
      OptionT {
        IO.fromFuture { IO { ProtectedResource.handleRequest(resourceReq, TestTokenEndpoint) } }
          .map { _.toOption.map(_.user) }
      }
    }

  val publicServices = HttpRoutes.of[IO] {
    case request @ POST -> Root / "access_token" =>
      request.decode[UrlForm] { formData =>
        val formMap = formData.values.mapValues(_.toVector)
        val authReq = new AuthorizationRequest(headerMap(request), formMap)
        IO.fromFuture { IO(TestTokenEndpoint.handleRequest(authReq, TestTokenEndpoint)) }.flatMap {
          case Left(err) => Forbidden(err.description)
          case Right(result) =>
            Ok(
              OAuthTokenResponse(result.accessToken,
                                 result.refreshToken,
                                 Some(result.tokenType),
                                 expires_in = result.expiresIn,
                                 None).asJson)
        }
      }
    case request @ GET -> Root / "control.js" => {
      StaticFile
        .fromResource[IO]("/www/control.js", ExecutionContext.global)
        .getOrElse(Response.notFound)
    }
  }

  val middleware: AuthMiddleware[IO, TestUser] =
    AuthMiddleware(authUser)

  val protectedService = AuthedService[TestUser, IO] {
    case GET -> Root / "controls" as user =>
      Ok(
        Map(
          s"testcontrol" ->
            RProviderControlDefinition(
              s"Lovely control ${user.clientId}",
              None,
              sampleConfig
            )).asJson)
  }

  val oauthService = publicServices <+> middleware(protectedService)

  val tokenMap = new ConcurrentHashMap[String, (TestUser, AccessToken)].asScala

  object TestTokenEndpoint extends TokenEndpoint with DataHandler[TestUser] {

    override val handlers = Map("client_credentials" -> new ClientCredentials)

    override def validateClient(maybeCredential: Option[ClientCredential],
                                request: AuthorizationRequest): Future[Boolean] = {
      Future.successful(true)
    }

    override def findUser(maybeCredential: Option[ClientCredential],
                          request: AuthorizationRequest): Future[Option[TestUser]] = {
      Future.successful(maybeCredential.map(cc => TestUser(cc.clientId)))
    }

    override def createAccessToken(authInfo: AuthInfo[TestUser]): Future[AccessToken] = {
      Future.successful {
        val newToken    = UUID.randomUUID().toString
        val accessToken = AccessToken(newToken, None, None, Some(2000), new java.util.Date())
        tokenMap.put(newToken, (authInfo.user, accessToken))
        accessToken
      }
    }

    override def getStoredAccessToken(authInfo: AuthInfo[TestUser]): Future[Option[AccessToken]] = {
      Future.successful(None)
    }

    override def refreshAccessToken(authInfo: AuthInfo[TestUser],
                                    refreshToken: String): Future[AccessToken] = ???

    override def findAuthInfoByCode(code: String): Future[Option[AuthInfo[TestUser]]] = ???

    override def deleteAuthCode(code: String): Future[Unit] = ???

    override def findAuthInfoByRefreshToken(
        refreshToken: String): Future[Option[AuthInfo[TestUser]]] = ???

    override def findAuthInfoByAccessToken(
        accessToken: AccessToken): Future[Option[AuthInfo[TestUser]]] = {
      Future.successful {
        tokenMap.get(accessToken.token).map {
          case (u, at) => AuthInfo(u, Some(u.clientId), None, None)
        }
      }
    }

    override def findAccessToken(token: String): Future[Option[AccessToken]] = {
      Future.successful {
        tokenMap.get(token).map(_._2)
      }
    }

  }
}
