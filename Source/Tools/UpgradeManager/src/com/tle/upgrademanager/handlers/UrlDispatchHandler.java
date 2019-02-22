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

import com.sun.net.httpserver.HttpExchange;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class UrlDispatchHandler extends AbstractDispatchHandler {
  private Pattern parser;
  private String context;

  private synchronized Pattern getParser(HttpExchange exchange) {
    if (parser == null) {
      context = exchange.getHttpContext().getPath();
      parser = Pattern.compile("^" + context + "([^?/]*).*?$"); // $NON-NLS-1$ //$NON-NLS-2$
    }
    return parser;
  }

  @Override
  @SuppressWarnings("nls")
  public void doHandle(HttpExchange exchange) throws Exception {
    Matcher m = getParser(exchange).matcher(exchange.getRequestURI().toString());
    if (!m.matches()) {
      // We should never get here unless the handler mapping differs from
      // the base path
      throw new Exception("Dispatch cannot be parsed");
    }

    invokeAction(exchange, m.group(1));
  }
}
