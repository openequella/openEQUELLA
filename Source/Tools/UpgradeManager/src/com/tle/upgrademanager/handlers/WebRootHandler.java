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

package com.tle.upgrademanager.handlers;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import com.google.common.io.ByteStreams;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

public class WebRootHandler implements HttpHandler {
  @Override
  public void handle(HttpExchange exchange) throws IOException {
    String uri = exchange.getRequestURI().toString();
    if (uri.equals("/")) // $NON-NLS-1$
    {
      HttpExchangeUtils.respondRedirect(exchange, "/pages/"); // $NON-NLS-1$
      return;
    }
    URL res = getClass().getResource("/web" + uri); // $NON-NLS-1$
    if (res == null) {
      HttpExchangeUtils.respondFileNotFound(exchange);
      return;
    }

    URLConnection conn = res.openConnection();
    try (InputStream in = conn.getInputStream()) {
      HttpExchangeUtils.setContentType(exchange, HttpExchangeUtils.getContentTypeForUri(uri));
      exchange.sendResponseHeaders(200, conn.getContentLength());
      ByteStreams.copy(in, exchange.getResponseBody());
    } catch (Exception ex) {
      HttpExchangeUtils.respondApplicationError(exchange, ex);
    } finally {
      exchange.close();
    }
  }
}
