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
import sttp.client3.{asString, basicRequest}
import sttp.model.StatusCode

/**
  * The `StatusApi` object provides a client for the /api/status endpoints of the openEQUELLA REST API.
  */
object StatusApi {
  private val LOGGER: Logger = LoggerFactory.getLogger(StatusApi.getClass)
  private val API_PATH       = "status"

  /**
    * Update the heartbeat for the current user. Typically used to keep the session alive.
    */
  def heartbeat(implicit cfg: RestConfiguration): Either[RestError, Unit] = {
    sendWithCookies(cfg) {
      basicRequest.get(cfg.apiUrl().addPath(API_PATH, "heartbeat")).response(asString)
    } match {
      case response if response.code == StatusCode.Ok =>
        LOGGER.debug("Heartbeat successful")
        Right(())
      case response =>
        LOGGER.error(s"Heartbeat failed with status code ${response.code}")
        Left(StatusCodeError(s"Request for heartbeat results in non-OK status code", response.code))
    }
  }
}
