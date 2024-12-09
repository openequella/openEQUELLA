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

package com.tle.web.jwks

import com.tle.core.guice.Bind
import com.tle.core.webkeyset.service.WebKeySetService
import javax.inject.{Inject, Singleton}
import javax.servlet.http.{HttpServlet, HttpServletRequest, HttpServletResponse}

/** This Servlet responds to GET requests coming from `/.well-known/jwks.json` which is the standard
  * endpoint for web servers to publish JSON Web Key Sets(JWKS).
  *
  * The response content is a string in JSON format to represent a set of keys containing the public
  * keys used to verify any JSON Web Token (JWT) issued.
  *
  * Reference links:
  *   - https://www.rfc-editor.org/rfc/rfc8414
  *   - https://auth0.com/docs/secure/tokens/json-web-tokens/json-web-key-sets
  */
@Bind
@Singleton
class JwksServlet extends HttpServlet {
  @Inject var webKeySetService: WebKeySetService = _

  override protected def doGet(req: HttpServletRequest, resp: HttpServletResponse): Unit = {
    resp.setContentType("application/json")
    val out = resp.getWriter
    out.print(webKeySetService.generateJWKS)
    out.flush()
  }
}
