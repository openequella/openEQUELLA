/** Created by jolz on 26/04/17.
  */
package integtester

import cats.effect.kernel.Deferred
import cats.effect.unsafe.implicits.global
import cats.effect.{ExitCode, IO, IOApp}
import integtester.oauthredirector.OAuthRedirector
import integtester.oidc.OidcIntegration
import integtester.testprovider.TestingCloudProvider
import io.circe.syntax._
import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.http4s.headers._
import org.http4s.server.Router
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.server.staticcontent.ResourceServiceBuilder
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import org.slf4j.LoggerFactory

object IntegTester extends IOApp with Http4sDsl[IO] {

  val Logger = LoggerFactory.getLogger("IntegTester")

  val viewItemDocument = {
    val inpStream = getClass.getResourceAsStream(s"/www/viewitem.html")
    val htmlDoc   = Jsoup.parse(inpStream, "UTF-8", "")
    inpStream.close()
    htmlDoc
  }

  def viewItemHtml(request: Request[IO]): IO[Response[IO]] =
    request.decode[UrlForm] { form =>
      val formJson = form.values.view.mapValues(_.toVector) ++ request.uri.query.multiParams ++ Seq(
        "authenticated" ->
          Seq(request.headers.get[Authorization].isDefined.toString)
      )

      val doc = viewItemDocument.clone()
      doc
        .body()
        .insertChildren(
          0,
          new Element("script").text(s"var postValues = ${formJson.asJson.noSpaces}")
        )
      Ok(doc.toString, `Content-Type`(MediaType.text.html))
    }

  val integDocument = {
    val inpStream = getClass.getResourceAsStream(s"/www/integtester.html")
    val htmlDoc   = Jsoup.parse(inpStream, "UTF-8", "")
    inpStream.close()
    htmlDoc
  }

  def appHtml(request: Request[IO], dir: String = ""): IO[Response[IO]] = request.decode[UrlForm] {
    form =>
      val formJson =
        (form.values.view
          .mapValues(_.toVector)
          .toMap ++ request.uri.query.multiParams).asJson.noSpaces
      val doc = integDocument.clone()
      doc.body().insertChildren(0, new Element("script").text(s"var postValues = $formJson"))
      Ok(doc.toString, `Content-Type`(MediaType.text.html))
  }

  def echoServer(request: Request[IO]): IO[Response[IO]] = {
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
      </html>""",
      `Content-Type`(MediaType.text.html)
    )
  }

  def oidcService: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case req @ POST -> Root / "token" =>
      OidcIntegration.token(req)
    case req @ GET -> Root / "authorise" =>
      OidcIntegration.login(req)
    case _ @GET -> Root / ".well-known" / "jwks.json" =>
      OidcIntegration.jwks
    case req @ GET -> Root / "api" / "users" / uuid =>
      OidcIntegration.user
    case req @ GET -> Root / "api" / "users" =>
      OidcIntegration.users
    case _ => NotFound("Unknown OIDC Integration endpoint")
  }

  val appService = HttpRoutes.of[IO] {
    case request @ (GET | POST) -> Root / "index.html"        => appHtml(request)
    case request @ (GET | POST) -> Root / "viewitem.html"     => viewItemHtml(request)
    case request @ (GET | POST) -> Root / "echo" / "index.do" => echoServer(request)
    case request @ (GET | POST) -> Root / "oauthredirector" =>
      OAuthRedirector.oauthRedirector(request)
    case request @ (GET | POST) -> Root / "provider/" => appHtml(request)
  }

  def buildServer(args: List[String]) = {
    val httpApp: HttpApp[IO] = Router(
      "/"          -> ResourceServiceBuilder[IO](basePath = "/www").toRoutes,
      "/"          -> appService,
      "/provider/" -> new TestingCloudProvider().oauthService,
      "/oidc/"     -> oidcService
    ).orNotFound

    BlazeServerBuilder[IO]
      .bindHttp(8083, "0.0.0.0")
      .withHttpApp(httpApp)
  }

  def integTesterUrl: String = "http://localhost:8083/index.html"

  def echoServerUrl: String = "http://localhost:8083/echo"

  def providerRegistrationUrl: String = "http://localhost:8083/provider.html"

  /** Starts the HTTP server and keeps it running until the JVM is terminated. The stream does not
    * emit any values and runs indefinitely.
    */
  override def run(args: List[String]): IO[ExitCode] =
    buildServer(args).serve.compile.drain.as(ExitCode.Success)

  /** Starts the HTTP server in the background and returns a function to stop it.
    *
    * This method is intended for use in test environments where the server needs to be started
    * before running tests and shut down afterward.
    *
    * A `Deferred` signal is used to ensure the server is started before the test execution
    * continues. The returned function can be called to cancel the shut down the server.
    *
    * @return
    *   a `() => Unit` function that, when invoked, stops the running server
    */
  def start(): () => Unit = {
    // Primitive used to indicate if the server has been started.
    val started: Deferred[IO, Unit] = Deferred.unsafe[IO, Unit]

    def startServer =
      for {
        server <- buildServer(List.empty).resource
          .evalTap(_ => started.complete(())) // Signal once server is started
          .useForever // Creates a long-running IO[Unit] that never completes
          .start      // Runs the IO which returns a Fibre that represents the running computation.
        _ <- started.get // Wait until a signal is received
      } yield server

    // Unsafe run the server eventually.
    val server = startServer.unsafeRunSync()

    // Function to stop the server
    val stopServer = () => server.cancel.unsafeRunSync()
    stopServer
  }
}
