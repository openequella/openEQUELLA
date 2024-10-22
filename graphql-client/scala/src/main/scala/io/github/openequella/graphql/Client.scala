package io.github.openequella.graphql

import caliban.client.Operations.IsOperation
import caliban.client.{CalibanClientError, GraphQLResponseError, Operations, SelectionBuilder}
import io.github.openequella.graphql.api.{ApiError, ApiErrorCause, GraphQlError}
import sttp.client3.{Request, SimpleHttpClient, asString, basicRequest}
import sttp.model.headers.CookieWithMeta
import sttp.model.{MediaType, StatusCode, Uri}

case class ClientConfiguration(institutionUrl: Uri,
                               cookies: scala.collection.mutable.Set[CookieWithMeta] =
                                 scala.collection.mutable.Set.empty)

case class ServerResponse[A](data: A, responseErrors: List[GraphQLResponseError])

object Client {
  private val GRAPHQL_PATH = Seq("graphql")
  private val LOGIN_PATH   = Seq("api", "auth", "login")

  def query[R](request: SelectionBuilder[Operations.RootQuery, R])(
      implicit cfg: ClientConfiguration
  ): Either[List[ApiError], R] = sendRequest(request)

  def mutate[R](request: SelectionBuilder[Operations.RootMutation, R])(
      implicit cfg: ClientConfiguration
  ): Either[List[ApiError], R] = sendRequest(request)

  /**
    * Login to the specified institution with the given credentials.
    */
  def login(username: String, password: String)(
      implicit cfg: ClientConfiguration): Either[(StatusCode, String), Unit] = {
    val request = basicRequest
      .post(
        cfg.institutionUrl
          .addPath(LOGIN_PATH)
          .addParams("username" -> username, "password" -> password))
      .response(asString)

    SimpleHttpClient().send(request) match {
      case response if response.code == StatusCode.Ok =>
        // There is a slight short falling in this API. If you target an institution URL that is invalid,
        // you still get a 200 back with a cookie. Ideally, the API needs to reply with a body as well
        // so that we can confirm we have actually logged in.
        cfg.cookies ++= response.cookies.collect {
          case Right(cookie) => cookie
        }
        Right(())
      case response =>
        Left((response.code, s"Login failed with status code ${response.code}"))
    }
  }

  /**
    * Whereas `send` is at the lower level of using sttp to send a HTTP request, `sendRequest` is
    * at the higher level of sending a request and handling the response. This is where the
    * Caliban-specific code is.
    */
  private def sendRequest[O, R](request: SelectionBuilder[O, R])(
      implicit cfg: ClientConfiguration,
      ev: IsOperation[O]
  ): Either[List[ApiError], R] = {

    /**
      * Extract the cause from a GraphQL response error. This is a custom extension that we add to
      * our GraphQL responses to provide more information about the error. Unfortunately, it's kept
      * abstract in the caliban data model, so we have to do some manual extraction.
      */
    def extractCause(error: GraphQLResponseError): Option[String] = {
      import caliban.client.__Value._
      error.extensions match {
        case Some(extensions) =>
          extensions match {
            // We expect 'extensions' property in the response from oEQ to be an object
            case __ObjectValue(fields) =>
              // Within the object, we expect there to be a 'cause' string property
              fields.toMap.get("cause") match {
                case Some(__StringValue(cause)) => Some(cause)
                case Some(_)                    => None
                case None                       => None
              }
            // We have no interest in other types of 'extension' properties
            case _ => None
          }
        // If there are no 'extensions' property, we have no cause to extract
        case None => None
      }
    }

    // Convert the request to a sttp request and send it. Then convert the response to a ServerResponse.
    val response: Either[CalibanClientError, ServerResponse[R]] = send(cfg.cookies) {
      request.toRequestWith(cfg.institutionUrl.addPath(GRAPHQL_PATH)) { (a, errors, _) =>
        ServerResponse(a, errors)
      }
    }
    // If the response is a success, we need to check if there are any errors in the response. (As
    // a that's just success at the HTTP level, not the business logic and GraphQL/Caliban level.)
    response match {
      // First, there could be business logic level errors in the response
      case Right(serverResponse) if serverResponse.responseErrors.nonEmpty =>
        Left(serverResponse.responseErrors.map(error => {
          val errorCause =
            extractCause(error).flatMap(ApiErrorCause.fromString).getOrElse(ApiErrorCause.UNKNOWN)
          api.ApiError(errorCause, error.message)
        }))
      // If there are no business logic errors, we can return the data
      case Right(serverResponse) =>
        Right(serverResponse.data)
      // If there's an error at the Caliban level, that's most likely at the GraphQL level
      case Left(error) => Left(List(GraphQlError(error.getMessage())))
    }
  }

  /**
    * Basic sending of a request and handling of cookies. Nothing in here should be specific to the
    * library used for GraphQL.
    *
    * @param cookieJar The cookie jar to use for the request, and to update with any new cookies.
    * @param request The request to send.
    * @tparam T The type of the response body.
    * @return The response body.
    */
  private def send[T](cookieJar: scala.collection.mutable.Set[CookieWithMeta])(
      request: Request[T, Any]): T = {
    val result =
      SimpleHttpClient().send(request.contentType(MediaType.ApplicationJson).cookies(cookieJar))

    val cookies = result.cookies.collect {
      case Right(cookie) => cookie
    }
    cookieJar ++= cookies

    result.body
  }
}
