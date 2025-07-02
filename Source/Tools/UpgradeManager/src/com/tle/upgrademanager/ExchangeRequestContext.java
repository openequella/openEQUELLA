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

package com.tle.upgrademanager;

import com.sun.net.httpserver.HttpExchange;
import com.tle.upgrademanager.handlers.HttpExchangeUtils;
import java.io.IOException;
import java.io.InputStream;
// import org.apache.commons.fileupload.RequestContext;
import org.apache.commons.fileupload2.core.RequestContext;

/** */
public class ExchangeRequestContext implements RequestContext {
  private final HttpExchange exchange;

  public ExchangeRequestContext(HttpExchange exchange) {
    this.exchange = exchange;
  }

  @Override
  public String getCharacterEncoding() {
    return null;
  }

  @Override
  public long getContentLength() {
    String contentLength = exchange.getRequestHeaders().getFirst("Content-Length");
    if (contentLength != null && contentLength.length() > 0) {
      try {
        return Integer.parseInt(contentLength);
      } catch (NumberFormatException n) {
        // nada
      }
    }
    return -1;
  }

  @Override
  public String getContentType() {
    return HttpExchangeUtils.getContentType(exchange);
  }

  @Override
  public InputStream getInputStream() throws IOException {
    return exchange.getRequestBody();
  }

  @Override
  public boolean isMultipartRelated() {
    return false;
  }
}
