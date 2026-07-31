package equellatests.restapi

import cats.data.{Kleisli, NonEmptyList}
import cats.effect.IO
import cats.effect.unsafe.implicits.global
import cats.free.Free
import cats.~>
import com.tle.webtests.framework.PageContext
import io.circe.parser._
import io.circe.syntax._
import io.circe.{Decoder, Encoder, Json}
import org.http4s.blaze.client.BlazeClientBuilder
import org.http4s.circe._
import org.http4s.client.Client
import org.http4s.{Headers, Method, Request, RequestCookie, Response, Status, Uri, headers}

import scala.jdk.CollectionConverters._

sealed trait ERestA[A]

case class ERequest[A](
    method: Method,
    uri: Uri,
    params: Map[String, Seq[String]],
    body: Option[Json],
    f: Response[IO] => IO[A]
) extends ERestA[A]
case class ERelativeUri(fullUri: Uri, base: Uri) extends ERestA[Option[Uri]]

object ERest {
  val (client: Client[IO], shutdownClient: IO[Unit]) = {
    val builder        = BlazeClientBuilder[IO]
    val clientResource = builder.resource
    clientResource.allocated.unsafeRunSync()
  }

  case class ReqContext(base: Uri, cookies: NonEmptyList[RequestCookie])

  type PageIO[A] = Kleisli[IO, ReqContext, A]

  def pageIO[A](f: ReqContext => IO[A]): PageIO[A] = Kleisli(f)

  val pureCompiler: ERestA ~> PageIO = new (ERestA ~> PageIO) {
    def apply[A](fa: ERestA[A]): PageIO[A] =
      fa match {
        case ERequest(method, uri, params, body, f) =>
          pageIO { ctx =>
            val reqUri = ctx.base.resolve(uri).setQueryParams(params)
            val _req = Request[IO](method, reqUri).withHeaders(Headers(headers.Cookie(ctx.cookies)))
            val req  = body.map(j => IO.pure(_req.withEntity(j))).getOrElse(IO.pure(_req))
            client.fetch(req)(resp => f(resp))
          }
        case ERelativeUri(uri, base) =>
          pageIO { ctx =>
            IO.pure {
              val basePath = ctx.base.resolve(base).path.renderString
              val fullPath = uri.path.renderString
              if (fullPath.startsWith(basePath)) {
                Some(Uri.unsafeFromString(fullPath.substring(basePath.length)))
              } else None
            }
          }
      }
  }

  def run[A](ctx: PageContext)(er: ERest[A]): A = {
    val cookies = NonEmptyList.fromListUnsafe(
      ctx.getDriver
        .manage()
        .getCookies
        .asScala
        .map(c => RequestCookie(c.getName, c.getValue))
        .toList
    )
    val reqctx = ReqContext(Uri.unsafeFromString(ctx.getBaseUrl), cookies)
    er.foldMap(pureCompiler).run(reqctx).unsafeRunSync()
  }

  def get[A: Decoder](uri: Uri, params: Map[String, Seq[String]] = Map.empty): ERest[A] =
    Free.liftF {
      ERequest(
        Method.GET,
        uri,
        params,
        None,
        resp =>
          jsonDecoder[IO]
            .decode(resp, false)
            .flatMapF(j => IO.pure(j.as[A]))
            .fold(throw _, identity)
      )
    }

  def relative(fullUri: Uri, base: Uri): ERest[Option[Uri]] =
    Free.liftF(ERelativeUri(fullUri, base))

  def postCheckHeaders[A: Encoder](
      uri: Uri,
      a: A,
      params: Map[String, Seq[String]] = Map.empty
  ): ERest[(Status, Headers)] = Free.liftF {
    ERequest(
      Method.POST,
      uri,
      params,
      Some(a.asJson),
      resp => resp.body.compile.drain.map(_ => (resp.status, resp.headers))
    )
  }

  def postEmpty[A](uri: Uri, params: Map[String, Seq[String]] = Map.empty)(implicit
      dec: Decoder[A]
  ): ERest[A] =
    Free.liftF {
      ERequest(
        Method.POST,
        uri,
        params,
        None,
        resp =>
          resp.body
            .through(fs2.text.utf8Decode)
            .compile
            .lastOrError
            .map(parse)
            .map(_.flatMap(dec.decodeJson).fold(throw _, identity))
      )
    }
}
