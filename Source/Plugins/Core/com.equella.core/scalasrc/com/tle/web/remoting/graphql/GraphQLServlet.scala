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

package com.tle.web.remoting.graphql

import caliban.CalibanError.ExecutionError
import caliban.{CalibanError, GraphQL, GraphQLRequest}
import com.google.common.io.ByteStreams
import com.tle.core.guice.Bind
import com.tle.web.remoting.graphql.ErrorCode.{ACCESS_DENIED, INTERNAL_ERROR, IO_ERROR}
import com.tle.web.remoting.graphql.Errors.mapException
import com.tle.web.remoting.graphql.GraphQLConfig.{CFG_GRAPHQL_SCHEMA, CFG_GRAPHQL_UI}
import com.tle.web.remoting.graphql.schema.Schema
import io.circe.parser._
import org.slf4j.LoggerFactory

import java.util.UUID
import javax.inject.{Inject, Named, Singleton}
import javax.servlet.http.{HttpServlet, HttpServletRequest, HttpServletResponse}
import javax.ws.rs.core.MediaType
import scala.io.Source

/**
  * Servlet for handling GraphQL requests. Relies on standard oEQ servlet security/authentication
  * filters/mechanisms.
  */
@Bind
@Singleton
class GraphQLServlet extends HttpServlet {
  private val LOGGER = LoggerFactory.getLogger(classOf[GraphQLServlet])

  private var graphQL: GraphQL[Any]          = _
  private var schemaEndpointEnabled: Boolean = false
  private var graphQLUIEnabled: Boolean      = false

  /**
    * Default constructor for Guice.
    */
  @Inject def this(schema: Schema,
                   @Named(CFG_GRAPHQL_SCHEMA) graphQLSchemaEnabled: Boolean,
                   @Named(CFG_GRAPHQL_UI) graphQLUIEnabled: Boolean) = {
    this()
    this.graphQL = schema.getFullApi
    this.schemaEndpointEnabled = graphQLSchemaEnabled
    this.graphQLUIEnabled = graphQLUIEnabled
  }

  override def doGet(req: HttpServletRequest, resp: HttpServletResponse): Unit = {
    LOGGER.debug(s"doGet(${req.getServletPath}) called")

    req.getServletPath match {
      case "/graphql/schema" if schemaEndpointEnabled =>
        resp.setStatus(HttpServletResponse.SC_OK)
        resp.setContentType("text/plain")
        resp.getWriter.write(graphQL.render)
      case "/graphql/ui" if graphQLUIEnabled =>
        LOGGER.debug(s"UI path: ${req.getPathInfo}")
        graphiQL(resp, req.getPathInfo)
      case _ => resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "GET requests are not supported")
    }
  }

  /**
    * Add support for browser pre-flight requests.
    */
  override def doOptions(req: HttpServletRequest, resp: HttpServletResponse): Unit = {
    resp.setHeader("Access-Control-Allow-Origin", "*")
    resp.setHeader("Access-Control-Allow-Methods", "POST, OPTIONS")
    resp.setHeader("Access-Control-Allow-Headers", "Content-Type")
    resp.setHeader("Access-Control-Max-Age", "86400")

    super.doOptions(req, resp)
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

    if (req.getHeader("Content-Type") != MediaType.APPLICATION_JSON) {
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                     s"Content-Type must be ${MediaType.APPLICATION_JSON}")
      return
    }

    try {
      val query = Source.fromInputStream(req.getInputStream).mkString
      val processing = for {
        gqlRequest <- decode[GraphQLRequest](query)
        result     <- execute(gqlRequest)
      } yield result

      processing match {
        case Left(error) =>
          LOGGER.error("Error executing query", error)
          resp.setStatus(HttpServletResponse.SC_BAD_REQUEST)
        case Right(result) =>
          resp.setStatus(HttpServletResponse.SC_OK)
          resp.setContentType(MediaType.APPLICATION_JSON)
          resp.setHeader("Access-Control-Allow-Origin", "*")
          resp.getWriter.write(result)
      }
    } catch {
      case e: Exception =>
        // UUID for easy tracking of the error in logs to client side
        val msg = s"Error executing query (${UUID.randomUUID()})"
        LOGGER.error(msg, e)

        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR)
        resp.setContentType(MediaType.APPLICATION_JSON)
        resp.getWriter.write(s"""{"errors": [{"message": "$msg"}]}""")
    }
  }

  import zio._

  /**
    * Executes a GraphQL request and returns the result as a JSON string.
    */
  private def execute(req: GraphQLRequest): Either[Throwable, String] = zio.Unsafe.unsafe {
    implicit unsafe =>
      val calibanExecute = for {
        // Setup the interpreter with `mapError` so that we can customise the error handling and
        // provide more informative errors to users. Caliban defaults to a generic "Effect failure"
        // message which is not helpful. See more in the Caliban FAQ at:
        // https://ghostdogpr.github.io/caliban/faq/#my-query-fails-with-an-effect-failure-error-how-can-i-get-more-details
        interpreter <- graphQL.interpreter.map(_.mapError(errorHandler))
        result      <- interpreter.executeRequest(req)
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
            val errorWithCause =
              e.copy(msg = cause.getMessage, extensions = Some(Errors.buildCauseObjectValue(cause)))
            mapException(cause) match {
              case ACCESS_DENIED =>
                LOGGER.error(s"Access denied: $errorWithCause")
              case INTERNAL_ERROR | IO_ERROR =>
                LOGGER.error(s"Internal error: ${errorWithCause.toResponseValue}", cause)
            }
            errorWithCause
          case _ => e
        }
      case otherError => otherError
    }
  }

  /**
    * Streams the requested GraphiQL file which is stored in the `graphql-ui` resources folder.
    *
    * @param resp The response object matching the servlet request
    * @param path The path to the requested file
    */
  private def graphiQL(resp: HttpServletResponse, path: String): Unit = {
    def extension(str: String): String = {
      val index = str.lastIndexOf(".")
      if (index == -1) str else str.substring(index + 1)
    }

    // Setup the path to point to a file within the GraphiQL resources stored in the `graphql-ui`
    // folder. If no path is provided, then start with the standard `index.html`.
    val graphiQLPath = "/graphql-ui" + (if (path.isEmpty || path == "/") "/index.html" else path)

    // There are more content types here than currently used by GraphiQL, but they are included for
    // completeness and future proofing.
    val contentType = extension(graphiQLPath) match {
      case "css"  => "text/css"
      case "html" => "text/html"
      case "ico"  => "image/x-icon"
      case "js"   => "application/javascript"
      case "json" => "application/json"
      case "map"  => "application/json"
      case "png"  => "image/png"
      case "svg"  => "image/svg+xml"
      case _      => "text/plain"
    }

    Option(getClass.getResourceAsStream(graphiQLPath)) match {
      case Some(stream) =>
        resp.setStatus(HttpServletResponse.SC_OK)
        resp.setContentType(contentType)
        resp.setContentLength(stream.available())

        ByteStreams.copy(stream, resp.getOutputStream)
      case None =>
        resp.sendError(HttpServletResponse.SC_NOT_FOUND)
    }
  }
}
