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

package com.tle.web.api

import com.tle.common.beans.exception.NotFoundException
import com.tle.exceptions.{AccessDeniedException, AuthenticationException}

import javax.ws.rs.core.Response
import javax.ws.rs.core.Response.Status

case class ApiResponseMessage(message: String)
case class ApiErrorResponse(errors: Seq[ApiResponseMessage])

/**
  * Helper object to centralise the creation of error responses and provide a general error handler.
  */
object ApiErrorResponse {

  def resourceNotFound(errors: String*): Response = {
    buildErrorResponse(Status.NOT_FOUND, errors)
  }

  def badRequest(errors: String*): Response = {
    buildErrorResponse(Status.BAD_REQUEST, errors)
  }

  def unauthorizedRequest(errors: String*): Response = {
    buildErrorResponse(Status.UNAUTHORIZED, errors)
  }

  def forbiddenRequest(errors: String*): Response = {
    buildErrorResponse(Status.FORBIDDEN, errors)
  }

  def serverError(errors: String*): Response = {
    buildErrorResponse(Status.INTERNAL_SERVER_ERROR, errors)
  }

  def conflictError(errors: String*): Response = {
    buildErrorResponse(Status.CONFLICT, errors)
  }

  /**
    * Error handler to create an error response for an API request.
    * More exceptions will be supported as needed.
    *
    * @param error Error captured in a API request which will determine what error code to be returned.
    */
  def apiErrorHandler(error: Throwable): Response =
    error match {
      case e: NotFoundException        => resourceNotFound(e.getMessage)
      case e: IllegalArgumentException => badRequest(e.getMessage)
      case e: AccessDeniedException    => forbiddenRequest(e.getMessage)
      case e: AuthenticationException  => unauthorizedRequest(e.getMessage)
      case e                           => serverError(e.getMessage)
    }

  private def buildErrorResponse(status: Status, errors: Seq[String]): Response = {
    Response.status(status).entity(responseBody(errors)).build()
  }

  private def responseBody(errors: Seq[String]) = ApiErrorResponse(errors.map(ApiResponseMessage))
}
