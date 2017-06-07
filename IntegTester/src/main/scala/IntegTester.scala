/**
  * Created by jolz on 26/04/17.
  */

import java.nio.file.Paths

import org.http4s.dsl._
import org.http4s.server.staticcontent._
import org.http4s.server.blaze.BlazeBuilder
import org.http4s.util.StreamApp
import io.circe.syntax._
import io.circe.generic.auto._
import fs2._
import org.http4s.server.staticcontent.FileService.Config
import org.http4s.{HttpService, MediaType, Request, UrlForm}

object IntegTester extends StreamApp {

  def appHtml(request: Request, dir: String = "") = request.decode[UrlForm] {
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

  def echoServer(request: Request) = {
    val echo = request.uri.query.params.get("echo")
    Ok(s"""<html>
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


  val appService = HttpService {
    case request@(GET | POST) -> Root / "index.html" => appHtml(request)
    case request@(GET | POST) -> Root / "echo" / "index.do" => echoServer(request)
  }

  val filePath = sys.props.get("files")

  def stream(args: List[String]): fs2.Stream[Task, Nothing] =
    BlazeBuilder
      .bindHttp(8083, "0.0.0.0")
      .mountService(appService)
      .mountService(filePath.fold(resourceService(ResourceService.Config("/www")))(p => fileService(Config(p))))
      .serve

}

