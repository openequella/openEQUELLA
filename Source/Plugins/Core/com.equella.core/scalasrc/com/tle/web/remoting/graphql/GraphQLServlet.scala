package com.tle.web.remoting.graphql

import caliban.CalibanError.ExecutionError
import caliban.{CalibanError, GraphQL}
import com.tle.core.guice.Bind
import com.tle.web.remoting.graphql.schema.Schema
import org.slf4j.LoggerFactory

import javax.inject.{Inject, Singleton}
import javax.servlet.http.{HttpServlet, HttpServletRequest, HttpServletResponse}

@Bind
@Singleton
class GraphQLServlet extends HttpServlet {
  private val LOGGER = LoggerFactory.getLogger(classOf[GraphQLServlet])

  private var graphQL: GraphQL[Any] = _
  @Inject def this(schema: Schema) = {
    this()
    this.graphQL = schema.getFullApi
    Console.println(graphQL.render)
  }

  /**
    * Handles the POST request as per the [GraphQL-over-HTTP spec](https://graphql.github.io/graphql-over-http/draft/)
    * with a focus on section [5.4 POST Request](https://graphql.github.io/graphql-over-http/draft/#sec-POST).
    *
    * The body is expected to be JSON and as a result should have header `Content-Type: application/json`.
    *
    * The JSON object should have the following properties:
    * - `query`: A string GraphQL document to be executed.
    * - `operationName`: If the provided `query` contains multiple named operations, this specifies which operation to execute.
    * - `variables`: The runtime values to use for any GraphQL query variables as a JSON object.
    */
  override def doPost(req: HttpServletRequest, resp: HttpServletResponse): Unit = {
    LOGGER.debug("doPost() called")

    if (req.getHeader("Content-Type") != "application/json") {
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Content-Type must be application/json")
      return
    }

    val query = new String(req.getInputStream.readAllBytes())
    execute(query) match {
      case Left(error) =>
        LOGGER.error("Error executing query", error)
        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST)
      case Right(result) =>
        resp.setStatus(HttpServletResponse.SC_OK)
        resp.setContentType("application/json")
        resp.getWriter.write(result)
    }
  }

  import zio._

  private def execute(query: String): Either[Throwable, String] = zio.Unsafe.unsafe {
    implicit unsafe =>
      val calibanExecute = for {
        // Setup the interpreter with `mapError` so that we can customise the error handling and
        // provide more informative errors to users. Caliban defaults to a generic "Effect failure"
        // message which is not helpful. See more in the Caliban FAQ at:
        // https://ghostdogpr.github.io/caliban/faq/#my-query-fails-with-an-effect-failure-error-how-can-i-get-more-details
        interpreter <- graphQL.interpreter.map(_.mapError(errorHandler))
        result      <- interpreter.execute(query)
      } yield result

      Runtime.default.unsafe.run(calibanExecute).toEither.map(_.toResponseValue.toString)
  }

  /**
    * Handles errors that occur during the execution of a GraphQL query, rather than simply ending
    * up with error responses with a message of "Effect failure".
    */
  private def errorHandler(error: CalibanError): CalibanError = {
    error match {
      case e: ExecutionError =>
        e.innerThrowable match {
          case Some(cause: Throwable) =>
            e.copy(msg = cause.getMessage, extensions = Some(Errors.buildCauseObjectValue(cause)))
          case _ => e
        }
      case otherError => otherError
    }
  }
}
