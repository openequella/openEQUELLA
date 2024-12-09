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

package io.github.openequella.graphql.api

import ApiErrorCause.Cause

/** Represents an error that occurred during an API operation. Matching the possible 'cause' values
  * returned by the server.
  */
object ApiErrorCause extends Enumeration {
  type Cause = Value
  val ACCESS_DENIED, BAD_REQUEST, CLIENT_ABORT, INTERNAL_ERROR, IO_ERROR, LOCKED, NOT_FOUND,
      UNKNOWN = Value

  /** Converts a string to a Cause.
    *
    * @param s
    *   The string to convert.
    * @return
    *   The Cause or None if the string does not match any Cause.
    */
  def fromString(s: String): Option[Cause] = values.find(_.toString == s)
}

trait HasCause {
  val cause: Cause
}

/** Represents an error that occurred during an API operation.
  */
sealed abstract class ApiError {
  val message: String
}

object ApiError {

  /** Creates an ApiError from a Cause and a message.
    *
    * @param cause
    *   The cause of the error.
    * @param message
    *   The message describing the error.
    * @return
    *   The ApiError.
    */
  def apply(cause: Cause, message: String): ApiError = {
    cause match {
      case ApiErrorCause.ACCESS_DENIED  => AccessDeniedError(message)
      case ApiErrorCause.BAD_REQUEST    => BadRequestError(message)
      case ApiErrorCause.CLIENT_ABORT   => ClientAbortError(message)
      case ApiErrorCause.INTERNAL_ERROR => InternalError(message)
      case ApiErrorCause.IO_ERROR       => IOError(message)
      case ApiErrorCause.LOCKED         => LockedError(message)
      case ApiErrorCause.NOT_FOUND      => NotFoundError(message)
      case ApiErrorCause.UNKNOWN        => UnknownError(message)
      case _                            => UnknownError(message)
    }
  }
}

/** Represents an error that occurred during a GraphQL operation.
  */
final case class GraphQlError(message: String) extends ApiError

/** Returned by the server if the user does not have permission to perform the operation.
  */
final case class AccessDeniedError(message: String) extends ApiError with HasCause {
  override val cause: Cause = ApiErrorCause.ACCESS_DENIED
}

final case class BadRequestError(message: String) extends ApiError with HasCause {
  override val cause: Cause = ApiErrorCause.BAD_REQUEST
}

final case class ClientAbortError(message: String) extends ApiError with HasCause {
  override val cause: Cause = ApiErrorCause.CLIENT_ABORT
}

/** Returned by the server if an internal error occurred during the operation.
  */
final case class InternalError(message: String) extends ApiError with HasCause {
  override val cause: Cause = ApiErrorCause.INTERNAL_ERROR
}

final case class IOError(message: String) extends ApiError with HasCause {
  override val cause: Cause = ApiErrorCause.IO_ERROR
}

final case class LockedError(message: String) extends ApiError with HasCause {
  override val cause: Cause = ApiErrorCause.LOCKED
}

/** Returned by the server if the requested resource was not found. Typically in the case of
  * requesting an operation on a resource that does not exist. Not for retrieval operations where
  * the specified resource does not exist.
  */
final case class NotFoundError(message: String) extends ApiError with HasCause {
  override val cause: Cause = ApiErrorCause.NOT_FOUND
}

/** Used to represent unexpected errors that occurred during an operation. Use `message` to provide
  * a description of the error.
  */
final case class UnknownError(message: String) extends ApiError with HasCause {
  override val cause: Cause = ApiErrorCause.UNKNOWN
}
