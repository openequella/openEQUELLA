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

package com.tle.upgrademanager.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.tle.upgrademanager.JSONService;
import com.tle.upgrademanager.helpers.AjaxMessage;
import com.tle.upgrademanager.helpers.AjaxState;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AjaxProgressHandler implements HttpHandler {
  private Pattern parser;
  private String context;
  private AjaxState ajaxState;

  public AjaxProgressHandler(AjaxState ajaxState) {
    this.ajaxState = ajaxState;
  }

  /**
   * What to do with this method? It is at pages handler as well! What's up with parser and context
   * being instance variables? They could be local.
   */
  private synchronized Pattern getParser(HttpExchange exchange) {
    if (parser == null) {
      context = exchange.getHttpContext().getPath();
      parser = Pattern.compile("^" + context + "([^?/]*).*?$"); // $NON-NLS-1$ //$NON-NLS-2$
    }
    return parser;
  }

  @Override
  public void handle(HttpExchange exchange) throws IOException {
    Matcher m = getParser(exchange).matcher(exchange.getRequestURI().toString());
    if (!m.matches()) {
      // We should never get here unless the handler mapping differs from
      // the base path
      throw new IOException("Dispatch cannot be parsed");
    }

    String ajaxId = m.group(1);

    List<AjaxMessage> ls = ajaxState.getListOfAllMessages(ajaxId);

    HttpExchangeUtils.setNoCaching(exchange);
    HttpExchangeUtils.respondWithContent(
        exchange, 200, "application/json", JSONService.toString(ls)); // $NON-NLS-1$
  }
}
