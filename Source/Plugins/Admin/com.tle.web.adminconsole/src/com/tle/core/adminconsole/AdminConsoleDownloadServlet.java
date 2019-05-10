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

package com.tle.core.adminconsole;

import com.tle.core.guice.Bind;
import com.tle.core.plugins.PluginService;
import com.tle.web.resources.AbstractResourcesServlet;
import java.io.IOException;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Mapped to /institution_url/adminconsole.jar Basically this servlet exists because the console
 * bootstrapper needs a stable location to download from, whereas other resources include the
 * Equella version number in the URL
 */
@Bind
public class AdminConsoleDownloadServlet extends AbstractResourcesServlet {

  @Inject private PluginService pluginService;

  public AdminConsoleDownloadServlet() {
    isCalculateETag = true;
  }

  @Override
  protected void service(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    response.setHeader("Cache-Control", "private, max-age=5, must-revalidate");
    service(request, response, "adminconsole.jar", "application/java-archive");
  }

  @Override
  public String getRootPath() {
    return "web/";
  }

  @Override
  public String getPluginId(HttpServletRequest request) {
    return pluginService.getPluginIdForObject(AdminConsoleDownloadServlet.class);
  }
}
