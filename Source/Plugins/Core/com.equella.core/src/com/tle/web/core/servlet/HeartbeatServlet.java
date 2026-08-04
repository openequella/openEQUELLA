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

package com.tle.web.core.servlet;

import com.tle.core.guice.Bind;
import java.io.IOException;
import java.io.Serial;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Serves {@code /invoke.heartbeat}, polled every 10 minutes by {@code scripts/heartbeat.js} (which
 * is included on every legacy page via {@code RenderTemplate}) to stop the user's session from
 * timing out while a page is left open, and to alert the user when the server becomes unreachable.
 *
 * <p>The response body is intentionally empty: simply receiving the request is enough, as the
 * servlet container resets the {@code HttpSession} inactivity timeout for any request associated
 * with the session (with the openEQUELLA user state bound to it by {@code TleSessionFilter}). The
 * REST equivalent used by the new UI and Admin Console is {@code /api/status/heartbeat} ({@code
 * ServerResource}), which works the same way.
 */
@Bind
@Singleton
public class HeartbeatServlet extends HttpServlet {
  @Serial private static final long serialVersionUID = 1L;

  @Override
  protected void service(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    // IE will cache this otherwise
    response.setHeader("Cache-Control", "no-cache");
    response.setStatus(HttpServletResponse.SC_OK);
    // FF3 will try to parse as XML if no content type sent
    response.setContentType("text/plain");
    response.getOutputStream().close();
  }
}
