/**
  * Created by jolz on 26/04/17.
  */

package integtester

import cats.effect.IO
import io.circe.syntax._
import fs2._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.staticcontent._
import org.http4s.server.blaze.BlazeBuilder
import org.http4s.server.staticcontent.FileService.Config
import org.http4s.{HttpService, MediaType, Request, Response, UrlForm}
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext.Implicits.global

object IntegTester extends StreamApp[IO] with Http4sDsl[IO] {

  val Logger = LoggerFactory.getLogger("IntegTester")

  def appHtml(request: Request[IO], dir: String = ""): IO[Response[IO]] = request.decode[UrlForm] {
    form =>
      val formJson = (form.values ++ request.uri.query.multiParams).asJson.noSpaces
      Ok(
        s"""<html>
          <head>
              <link rel="stylesheet" href="${dir}styles.css" type="text/css">
              <title>Integration Tester</title>
          </head>
          <body>
            <div id="app"></div>
            <script>var postValues = ${formJson}</script>
            <script src="${dir}app.js"></script>
          </body>
        </html>""").withType(MediaType.`text/html`)
  }

  def echoServer(request: Request[IO]) : IO[Response[IO]] = {
    val echo = request.uri.query.params.get("echo")
    Ok(
      s"""<html>
      <head>
        <link rel="stylesheet" href="styles.css" type="text/css">
          <title>Echo Server</title>
        </head>
        <body>
            <div class="formrow">
              <label>
                Text to echo:
              </label>
              <span id="toecho">

              </span>
            </div>

            <div class="formrow">
              <label>
                Echoed:
              </label>
              <span id="echoed">${echo.getOrElse("")}</span>
            </div>
        </body>
      </html>""").withType(MediaType.`text/html`)
  }


  val appService = HttpService[IO] {
    case request@(GET | POST) -> Root / "index.html" => appHtml(request)
    case request@(GET | POST) -> Root / "echo" / "index.do" => echoServer(request)
  }

  val filePath = sys.props.get("files")

  def stream(args: List[String], requestShutdown: IO[Unit]) =
    BlazeBuilder[IO]
      .bindHttp(8083, "0.0.0.0")
      .mountService(appService)
      .mountService(filePath.fold(resourceService[IO](ResourceService.Config("/www"))) { p =>
        Logger.info(s"Serving from $p")
        fileService(Config(p))
      })
      .serve

  lazy val embeddedRunning : Boolean = {
    stream(List.empty, IO.pure()).compile.drain.unsafeRunAsync(_ => ())
    true
  }

  def integTesterUrl : String = {
    embeddedRunning
    "http://localhost:8083/index.html"
  }

  def echoServerUrl : String = {
    embeddedRunning
    "http://localhost:8083/echo"
  }
}

