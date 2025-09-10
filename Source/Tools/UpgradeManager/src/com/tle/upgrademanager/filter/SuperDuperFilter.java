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

package com.tle.upgrademanager.filter;

import com.google.common.io.ByteStreams;
import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;
import com.tle.common.Pair;
import com.tle.upgrademanager.ExchangeRequestContext;
import com.tle.upgrademanager.handlers.HttpExchangeUtils;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.apache.commons.fileupload2.core.FileItemInput;
import org.apache.commons.fileupload2.core.FileItemInputIterator;
import org.apache.commons.fileupload2.javax.JavaxServletFileUpload;
import org.apache.commons.io.IOUtils;

public class SuperDuperFilter extends Filter {
  public static final String PARAMS_KEY = "parameters"; // $NON-NLS-1$
  public static final String MULTIPART_STREAMS_KEY = "multipart"; // $NON-NLS-1$

  private static final Pattern STRIP_PATH_FROM_FILENAME =
      Pattern.compile("^.*?([^/\\\\]*)$"); // $NON-NLS-1$

  @Override
  public String description() {
    return "Parse GET and POST params and get file uploads out of multipart request and store in"
        + " HttpExchange attributes";
  }

  @Override
  public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
    final Map<String, List<String>> params = parseGetParameters(exchange);
    Map<String, Pair<String, File>> streams = null;

    if (HttpExchangeUtils.isPost(exchange)) {
      if (HttpExchangeUtils.isMultipartContent(exchange)) {
        streams = new HashMap<String, Pair<String, File>>();

        try {
          FileItemInputIterator ii =
              new JavaxServletFileUpload().getItemIterator(new ExchangeRequestContext(exchange));
          while (ii.hasNext()) {
            final FileItemInput is = ii.next();
            final String name = is.getFieldName();
            try (InputStream stream = is.getInputStream()) {
              if (!is.isFormField()) {
                // IE passes through the full path of the file,
                // where as Firefox only passes through the
                // filename. We only need the filename, so
                // ensure that we string off anything that looks
                // like a path.
                final String filename =
                    STRIP_PATH_FROM_FILENAME.matcher(is.getName()).replaceFirst("$1");
                final File tempfile = File.createTempFile("equella-manager-upload", "tmp");
                tempfile.getParentFile().mkdirs();
                streams.put(name, new Pair<String, File>(filename, tempfile));

                try (OutputStream out = new BufferedOutputStream(new FileOutputStream(tempfile))) {
                  ByteStreams.copy(stream, out);
                }
              } else {
                addParam(params, name, IOUtils.toString(stream, StandardCharsets.UTF_8));
              }
            }
          }
        } catch (Exception t) {
          throw new RuntimeException(t);
        }
      } else {
        try (InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), "UTF-8")) {
          BufferedReader br = new BufferedReader(isr);
          String query = br.readLine();

          parseQuery(query, params);
        }
      }
    }

    exchange.setAttribute(PARAMS_KEY, params);
    exchange.setAttribute(MULTIPART_STREAMS_KEY, streams);
    // attributes seem to last the life of a session... I don't know why...
    exchange.setAttribute("error", null);

    chain.doFilter(exchange);
  }

  private void addParam(Map<String, List<String>> params, String paramName, String paramValue) {
    if (params.containsKey(paramName)) {
      final List<String> values = params.get(paramName);
      values.add(paramValue);
    } else {
      final List<String> values = new ArrayList<String>();
      values.add(paramValue);
      params.put(paramName, values);
    }
  }

  private Map<String, List<String>> parseGetParameters(HttpExchange exchange) {
    URI requestedUri = exchange.getRequestURI();
    String query = requestedUri.getRawQuery();
    return parseQuery(query, new HashMap<String, List<String>>());
  }

  protected Map<String, List<String>> parseQuery(String query, Map<String, List<String>> params) {
    if (query != null) {
      try {
        for (String pair : query.split("[&]")) // $NON-NLS-1$
        {
          final String param[] = pair.split("[=]"); // $NON-NLS-1$

          String paramName = ""; // $NON-NLS-1$
          String paramValue = ""; // $NON-NLS-1$
          if (param.length > 0) {
            paramName = URLDecoder.decode(param[0], "UTF-8"); // $NON-NLS-1$
          }

          if (param.length > 1) {
            paramValue = URLDecoder.decode(param[1], "UTF-8"); // $NON-NLS-1$
          }

          addParam(params, paramName, paramValue);
        }
      } catch (UnsupportedEncodingException e) {
        throw new RuntimeException(e);
      }
    }
    return params;
  }
}
