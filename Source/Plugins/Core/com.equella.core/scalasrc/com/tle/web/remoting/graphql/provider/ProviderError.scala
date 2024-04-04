package com.tle.web.remoting.graphql.provider

import caliban.CalibanError.ExecutionError
import com.tle.web.remoting.graphql.Errors

import scala.util.{Failure, Success}

/**
  * Details of why a Provider operation failed.
  *
  * @param message a human-readable message
  * @param cause a string error code (ideally from `ErrorCodes`)
  */
case class ProviderError(message: String, cause: String)

object ProviderError {

  /**
    * Create a `ProviderError` with the provided human readable message and the `Throwable` which
    * caused the error.
    *
    * @param message human readable message
    * @param cause the `Throwable` that caused the error, from which an error code will be extracted
    */
  def apply(message: String, cause: Throwable): ProviderError =
    ProviderError(message, Errors.mapException(cause))

  /**
    * Extractor for a `ProviderError` into an `ExecutionError`, where the message is the same and the
    * `extensions` contain the cause of the error.
    */
  def unapply(providerError: ProviderError): Option[ExecutionError] = {
    val extensions = Some(Errors.buildCauseObjectValue(providerError.cause))

    val result = Some(ExecutionError(msg = providerError.message, extensions = extensions))

    result
  }

  /**
    * Wraps a block of code that may throw an exception into an `Either` that can be used in a
    * GraphQL request. Simplifies the error handling in Providers.
    *
    * @param msg a human-readable message to include in the error if the block fails
    * @param producer the block of code to execute
    * @tparam A the result of the block on success
    * @return an `Either` containing the result or a `ProviderError` on failure
    */
  def Try[A](msg: String = "")(producer: => A): Either[ProviderError, A] =
    scala.util.Try(producer) match {
      case Success(result) => Right(result)
      case Failure(exception) =>
        Left(ProviderError(msg + exception.getMessage, exception))
    }
}
