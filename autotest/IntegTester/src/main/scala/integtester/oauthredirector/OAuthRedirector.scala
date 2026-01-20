package integtester.oauthredirector

import cats.effect.IO
import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.google.common.io.CharStreams
import org.http4s.dsl.Http4sDsl
import org.http4s.headers._
import org.http4s._
import java.io.InputStreamReader
import java.net.{URL, URLEncoder}
import java.util.UUID
import java.util.concurrent.{ConcurrentHashMap, ConcurrentMap}
import scala.util.{Failure, Success, Try, Using}

case class TestInfo(clientId: String, clientSecret: String, equellaUrl: String, state: String)
case class WebException(code: Int, error: String) extends RuntimeException

/** It is called by OAuthTest with an initial request which contains OAuth credentials as params
  * prefixed with 'test_', then it does the OAuth authentication with oEQ, and then finally presents
  * back a page with the results which the test then checks.
  */
object OAuthRedirector extends Http4sDsl[IO] {
  private val PARAM_STATE                              = "state"
  private val PARAM_CODE                               = "code"
  private val PARAM_RESPONSE_TYPE                      = "response_type"
  private val PARAM_ERROR                              = "error"
  private val PARAM_ERROR_DESCRIPTION                  = "error_description"
  private val session: ConcurrentMap[String, TestInfo] = new ConcurrentHashMap()

  private def writeMapResponse(vals: Map[String, String]): IO[Response[IO]] = {
    val writer = new StringBuilder
    writer.append("<html><body id=\"redirectresponse\">")

    vals.foreach { case (key, value) =>
      writer.append("<div>")
      writer.append(key + ": ")
      writer.append("<span id=\"")
      writer.append(key)
      writer.append("\">")
      writer.append(value)
      writer.append("</span></div>")
    }

    Ok(writer.toString(), `Content-Type`(MediaType.text.html))
  }

  private def urlEncode(value: String): String = URLEncoder.encode(value, "UTF-8")

  private def initialise(params: Map[String, String]): TestInfo = {
    val state: String = params.getOrElse(PARAM_STATE, UUID.randomUUID().toString)

    params.get("test_client_id") match {
      case Some(testClientId) =>
        val clientSecret: String = params.getOrElse("test_client_secret", "")
        val equellaUrl: String   = params.getOrElse("test_equella_url", "")

        val testInfo = TestInfo(testClientId, clientSecret, equellaUrl, state)
        session.put(state, testInfo)

        testInfo
      case None if state.nonEmpty => // This is a redirect back from oEQ and the state has been registered.
        val conf = session.get(state)
        TestInfo(conf.clientId, conf.clientSecret, conf.equellaUrl, state)
      case _ => throw WebException(400, "Client ID not supplied")
    }
  }

  private def processState(
      params: Map[String, String],
      state: String,
      equellaUrl: String,
      clientId: String,
      clientSecret: String,
      redirectUri: String
  ): IO[Response[IO]] = params.get(PARAM_CODE) match {
    case Some(code) if session.containsKey(state) =>
      // A response from oEQ has been received with the state previously provided,
      // now do the next step and request an access token using the provided auth code.
      val tokenUrl = new URL(
        s"${equellaUrl}oauth/access_token?grant_type=authorization_code&client_id=$clientId&redirect_uri=${urlEncode(redirectUri)}&client_secret=$clientSecret&code=${urlEncode(code)}"
      )
      val openConnection = tokenUrl.openConnection()
      openConnection.getDoInput

      val token = Using.resource(new InputStreamReader(openConnection.getInputStream)) {
        CharStreams.toString(_)
      }

      val mapper    = new ObjectMapper().registerModule(DefaultScalaModule)
      val rootNode  = mapper.readValue(token, classOf[JsonNode])
      val tokenData = rootNode.get("access_token").asText()

      session.remove(state)
      writeMapResponse(Map("access_token" -> tokenData))
    case None =>
      // undertake first OAuth redirect to authenticate to oEQ and register a state value.
      val responseType = params.getOrElse(PARAM_RESPONSE_TYPE, "code")
      val authUrl =
        s"${equellaUrl}oauth/authorise?response_type=$responseType&client_id=$clientId&redirect_uri=${urlEncode(redirectUri)}&state=${urlEncode(state)}"

      Found(authUrl).map(_.putHeaders(Location(Uri.unsafeFromString(authUrl))))
    case _ => throw WebException(403, "The state does not match. You may be a victim of CSRF.")
  }

  def oauthRedirector(request: Request[IO]): IO[Response[IO]] = {
    val params = request.params
    Try {
      val TestInfo(clientId, clientSecret, equellaUrl, state) = initialise(params)
      val redirect_uri = s"http://localhost:8083${request.uri.path}"

      params.get(PARAM_ERROR) match {
        case Some(error) =>
          val vals = Map(
            PARAM_ERROR             -> error,
            PARAM_ERROR_DESCRIPTION -> params.getOrElse(PARAM_ERROR_DESCRIPTION, "")
          )
          writeMapResponse(vals)
        case None =>
          processState(params, state, equellaUrl, clientId, clientSecret, redirect_uri)
      }
    } match {
      case Success(resp) => resp
      case Failure(ex: WebException) =>
        ex.code match {
          case 400 => BadRequest(ex.error)
          case 403 => Forbidden(ex.error)
        }
      case Failure(ex) => throw new RuntimeException(ex)
    }
  }
}
