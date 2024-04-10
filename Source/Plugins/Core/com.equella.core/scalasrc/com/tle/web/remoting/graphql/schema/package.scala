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
