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

import javax.ws.rs.core.Response
import javax.ws.rs.core.Response.Status

case class ApiResponseMessage(message: String)
case class ApiErrorResponse(errors: Seq[ApiResponseMessage])

object ApiErrorResponse {

  def resourceNotFound(errors: String*): Response = {
    Response.status(Status.NOT_FOUND).entity(responseBody(errors)).build()
  }

  def badRequest(errors: String*): Response = {
    Response.status(Status.BAD_REQUEST).entity(responseBody(errors)).build()
  }

  private def responseBody(errors: Seq[String]) = ApiErrorResponse(errors.map(ApiResponseMessage))
}
