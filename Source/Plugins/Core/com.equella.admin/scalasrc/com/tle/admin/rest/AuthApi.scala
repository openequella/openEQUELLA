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

package com.tle.admin.rest

import org.slf4j.{Logger, LoggerFactory}
import sttp.client3.basicRequest
import sttp.model.StatusCode

/**
  * The `AuthApi` object provides a client for the /api/auth endpoints of the openEQUELLA REST API.
  */
object AuthApi {
  private val LOGGER: Logger = LoggerFactory.getLogger(AuthApi.getClass)
  private val API_PATH       = "auth"

  /**
    * Terminates the session for the currently authenticated user.
    */
  def logout(implicit cfg: RestConfiguration): Either[RestError, Unit] = {
    sendWithCookies(cfg) {
      basicRequest.put(cfg.apiUrl().addPath(API_PATH, "logout"))
    } match {
      case response if response.code == StatusCode.Ok =>
        LOGGER.debug("Logout successful")
        Right(())
      case response =>
        LOGGER.error(s"Logout failed with status code ${response.code}")
        Left(StatusCodeError(s"Request for logout results in non-OK status code", response.code))
    }
  }
}
