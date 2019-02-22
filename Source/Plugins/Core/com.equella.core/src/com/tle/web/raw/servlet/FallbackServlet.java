/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.web.raw.servlet;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Singleton;
import com.tle.common.PathUtils;
import com.tle.core.guice.Bind;
import com.tle.core.plugins.PluginService;
import com.tle.web.resources.AbstractResourcesServlet;
import java.io.IOException;
import java.util.Map;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Bind
@Singleton
@SuppressWarnings("nls")
public class FallbackServlet extends AbstractResourcesServlet {
  private static final Map<String, String> CONTENT_TYPES =
      ImmutableMap.of(
          "js",
          "application/javascript",
          "css",
          "text/css",
          "gif",
          "image/gif",
          "png",
          "image/png");

  @Inject private PluginService pluginService;

  @Override
  protected void service(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    String pathInfo = request.getPathInfo();
    String mofo = request.getServletPath() + Strings.nullToEmpty(pathInfo);
    String mimeType = null;

    if (!Strings.isNullOrEmpty(pathInfo)) {
      String ext = PathUtils.extension(pathInfo);
      if (ext != null && CONTENT_TYPES.containsKey(ext)) {
        mimeType = CONTENT_TYPES.get(ext);
      }
    }
    service(request, response, mofo, mimeType);
  }

  @Override
  public String getRootPath() {
    return "web/";
  }

  @Override
  public String getPluginId(HttpServletRequest request) {
    return pluginService.getPluginIdForObject(FallbackServlet.class);
  }
}
