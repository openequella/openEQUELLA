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

package com.tle.admin

import sttp.client3.{Request, Response, SimpleHttpClient}

/** The `rest` package contains the REST API client for the TLE Admin Console to utilise the
  * openEQUELLA REST APIs. Support is only implemented for the parts for the API that the Admin
  * Console requires.
  */
package object rest {

  /** Sends a request with the cookies from the given configuration.
    *
    * @param cfg the REST configuration to use for the request
    * @param request the request to send
    * @tparam T the type of the response body
    * @return the response from the server
    */
  def sendWithCookies[T](cfg: RestConfiguration)(request: Request[T, Any]): Response[T] =
    SimpleHttpClient().send(request.cookies(cfg.cookies))

}
