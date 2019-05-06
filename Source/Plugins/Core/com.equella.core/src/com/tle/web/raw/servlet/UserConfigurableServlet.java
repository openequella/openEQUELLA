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

package com.tle.web.raw.servlet;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.io.CharSource;
import com.google.common.io.CharStreams;
import com.google.common.io.Resources;
import com.tle.core.guice.Bind;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;

/**
 * Mostly for OER who need to be able to edit a file (configurable.txt) and have it served up by
 * EQUELLA. E.g. you could set the contents of said file to "NOT READY" and a load balancer would
 * ignore this node.
 *
 * @author Aaron
 */
@SuppressWarnings("nls")
@Bind
@Singleton
public class UserConfigurableServlet extends HttpServlet {
  private static final Logger LOGGER = Logger.getLogger(UserConfigurableServlet.class);

  private static final String NO_CONTENT = "\0";

  private final Cache<String, String> responseCache =
      CacheBuilder.newBuilder().expireAfterWrite(15, TimeUnit.SECONDS).build();
  private final CacheLoader cacheLoader = new CacheLoader();

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    final String content;
    try {
      content = responseCache.get("", cacheLoader);
    } catch (ExecutionException e) {
      resp.setStatus(500);
      LOGGER.error("Error serving content", e);
      return;
    }

    if (NO_CONTENT.equals(content)) {
      resp.setStatus(404);
    } else {
      resp.setContentType("text/plain");
      resp.setStatus(200);
      CharStreams.copy(new StringReader(content), resp.getWriter());
    }
  }

  private class CacheLoader implements Callable<String> {
    private long lastMod;
    private String lastContent = NO_CONTENT;

    @Override
    public String call() throws Exception {
      try {
        final URL resource = Resources.getResource("configurable.txt");
        final File f = new File(resource.toURI());
        if (!f.exists()) {
          return NO_CONTENT;
        }
        if (lastMod == 0 || lastMod < f.lastModified()) {
          final CharSource charSource = Resources.asCharSource(resource, Charset.forName("utf-8"));
          final StringWriter sw = new StringWriter();
          charSource.copyTo(sw);
          lastContent = sw.toString();
          lastMod = f.lastModified();
        }
        return lastContent;
      } catch (Exception e) {
        return NO_CONTENT;
      }
    }
  }
}
