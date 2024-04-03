package com.tle.web.remoting.graphql

import caliban.CalibanError.ExecutionError
import com.tle.web.remoting.graphql.provider.ProviderError
import zio.{IO, ZIO}

package object schema {

  /**
    * A common return type for Providers to enable error handling in GraphQL requests.
    * @tparam T the success result from a Provider
    */
  type ResultWithErrors[T] = IO[ExecutionError, T]

  /**
    * Implicitly convert an `Either[ProviderError, T]` into a `ResultWithErrors[T]` to reduce the
    * repetition of error handling for Providers.
    *
    * @param eitherResult the result to convert - typically from a call to a Provider
    * @tparam T the success result from a Provider
    * @return a `ResultWithErrors[T]` that can be used in a GraphQL requests
    */
  implicit def errorHandler[T](eitherResult: Either[ProviderError, T]): ResultWithErrors[T] =
    eitherResult match {
      case Left(ProviderError(executionError: ExecutionError)) => ZIO.fail(executionError)
      case Right(data)                                         => ZIO.succeed(data)
    }
}
