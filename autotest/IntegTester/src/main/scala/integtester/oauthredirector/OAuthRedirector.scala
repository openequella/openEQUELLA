package integtester.oauthredirector

import cats.effect.IO
import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.google.common.collect.MapMaker
import com.google.common.io.CharStreams
import org.http4s.dsl.Http4sDsl
import org.http4s.headers._
import org.http4s._
import java.io.InputStreamReader
import java.net.{URL, URLEncoder}
import java.util.UUID
import java.util.concurrent.ConcurrentMap
import scala.util.Using

case class TestInfo(clientId: String, clientSecret: String, equellaUrl: String)

object OAuthRedirector extends Http4sDsl[IO] {
  private val PARAM_STATE                              = "state"
  private val PARAM_CODE                               = "code"
  private val PARAM_RESPONSE_TYPE                      = "response_type"
  private val PARAM_ERROR                              = "error"
  private val PARAM_ERROR_DESCRIPTION                  = "error_description"
  private val session: ConcurrentMap[String, TestInfo] = new MapMaker().makeMap()

  private def writeMapResponse(vals: Map[String, String]): IO[Response[IO]] = {
    val writer = new StringBuilder
    writer.append("<html><body id=\"redirectresponse\">")

    vals.foreach {
      case (key, value) =>
        writer.append("<div>")
        writer.append(ent(key + ": "))
        writer.append("<span id=\"")
        writer.append(ent(key))
        writer.append("\">")
        writer.append(ent(value))
        writer.append("</span></div>")
    }

    Ok(writer.toString(), `Content-Type`(MediaType.text.html))
  }

  private def urlEncode(value: String): String = URLEncoder.encode(value, "UTF-8")

  private def ent(szStr: String): String = {
    Option(szStr) match {
      case Some(str) =>
        val szOut              = new StringBuilder()
        val chars: Array[Char] = str.toCharArray

        chars.foreach {
          case '<' => szOut.append("&lt;")
          case '>' => szOut.append("&gt;")
          case '&' => szOut.append("&amp;")
          case '"' => szOut.append("&quot;")
          case other
              if other == 0xA || other == 0xD || other == 0x9 || (other >= 0x20 && other <= 0x007F) =>
            szOut.append(other)
          case other
              if (other > 0x007F && other <= 0xD7FF)
                || (other >= 0xE000 && other <= 0xFFFD)
                || (other >= 0x10000 && other <= 0x10FFFF) =>
            szOut.append("&#x")
            val hexed = Integer.toHexString(other)
            4 - hexed.length() match {
              case 3 => szOut.append('0')
              case 2 => szOut.append('0')
              case 1 => szOut.append('0')
              case _ => // nothing for else
            }
            szOut.append(hexed)
            szOut.append(';')
          case _ => // discard the character entirely
        }

        szOut.toString()
      case None => ""
    }
  }

  private def initialise(params: Map[String, String]) = {
    val state: String = params.getOrElse(PARAM_STATE, UUID.randomUUID().toString)

    params.get("test_client_id") match {
      case Some(testClientId) =>
        val clientSecret: String = params.getOrElse("test_client_secret", "")
        val equellaUrl: String   = params.getOrElse("test_equella_url", "")

        session.put(state, TestInfo(testClientId, clientSecret, equellaUrl))

        (testClientId, clientSecret, equellaUrl, state)
      case None if state.nonEmpty =>
        val conf = session.get(state)
        (conf.clientId, conf.clientSecret, conf.equellaUrl, state)
      case _ => throw new RuntimeException("test_client_id not supplied")
    }
  }

  private def processState(params: Map[String, String],
                           state: String,
                           equellaUrl: String,
                           clientId: String,
                           clientSecret: String,
                           redirectUri: String): IO[Response[IO]] = params.get(PARAM_CODE) match {
    case Some(code) if session.containsKey(state) =>
      val tokenUrl = new URL(
        s"${equellaUrl}oauth/access_token?grant_type=authorization_code&client_id=$clientId&redirect_uri=${urlEncode(
          redirectUri)}&client_secret=$clientSecret&code=${urlEncode(code)}")
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
      val responseType = params.getOrElse(PARAM_RESPONSE_TYPE, "code")
      val authUrl =
        Uri.unsafeFromString(
          s"${equellaUrl}oauth/authorise?response_type=$responseType&client_id=$clientId&redirect_uri=${urlEncode(
            redirectUri)}&state=${urlEncode(state)}")
      Found(authUrl).map(_.putHeaders(Location(authUrl)))
    case _ => throw new RuntimeException("The state does not match. You may be a victim of CSRF.");
  }

  def oauthRedirector(request: Request[IO]): IO[Response[IO]] = {
    val params                                      = request.params
    val (clientId, clientSecret, equellaUrl, state) = initialise(params)
    val redirect_uri                                = s"http://localhost:8083${request.uri.path}"

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
  }
}
