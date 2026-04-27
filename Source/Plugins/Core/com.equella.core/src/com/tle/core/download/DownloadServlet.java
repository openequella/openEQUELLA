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

package com.tle.core.download;

import com.tle.core.guice.Bind;
import com.tle.web.stream.ContentStreamWriter;
import com.tle.web.stream.FileContentStream;
import java.io.File;
import java.io.IOException;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HTTP servlet that serves JPF plugin JAR files to the Admin Console.
 *
 * <p>This servlet is mapped to the {@code /ds/*} URL pattern and acts as a simple HTTP download
 * endpoint for plugin JAR files. It is used in production environments where the Admin Console
 * needs to download plugins from the openEQUELLA server via HTTP.
 *
 * <p>The servlet works in conjunction with {@link PluginDownloadService}, which is responsible for
 * discovering plugins and rewriting their URLs to point to this servlet's endpoint. This servlet
 * simply serves the files when requested.
 *
 * <h2>Request Handling</h2>
 *
 * <ul>
 *   <li>Extracts the JAR filename from the request path (e.g., {@code /ds/plugin-name.jar})
 *   <li>Delegates to {@link PluginDownloadService#getFileForJar(String)} to locate the physical
 *       file on the filesystem
 *   <li>Streams the JAR file to the client with {@code application/java-archive} MIME type
 *   <li>Returns HTTP 404 if the requested JAR is not found
 * </ul>
 *
 * <h2>When This Servlet Is Used</h2>
 *
 * <p>This servlet is only invoked in production environments where plugins are packaged as JAR
 * files. In development environments, plugins are loaded directly from the filesystem using {@code
 * file:} URLs, so this servlet is never called.
 *
 * <p>For details on how plugin URLs are rewritten to use this servlet, see {@link
 * PluginDownloadService#getAllPluginDetails(String)}.
 *
 * @see PluginDownloadService
 */
@Bind
@Singleton
public class DownloadServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;

  private final Logger LOGGER = LoggerFactory.getLogger(DownloadServlet.class);

  @Inject private PluginDownloadService pluginDownloadService;
  @Inject private ContentStreamWriter contentStreamWriter;

  @Override
  protected void service(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    String pathInfo = request.getPathInfo().substring(1);
    LOGGER.debug(
        "DownloadServlet: Request received for plugin JAR: {} from IP: {}",
        pathInfo,
        request.getRemoteAddr());

    File file = pluginDownloadService.getFileForJar(pathInfo);
    if (file != null) {
      LOGGER.debug(
          "DownloadServlet: Serving file: {} (size: {} bytes)",
          file.getAbsolutePath(),
          file.length());

      FileContentStream stream =
          new FileContentStream(file, file.getName(), "application/java-archive"); // $NON-NLS-1$
      contentStreamWriter.outputStream(request, response, stream);
    } else {
      LOGGER.debug("DownloadServlet: File not found for JAR: {}", pathInfo);

      response.sendError(404);
    }
  }
}
