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

package com.tle.web.core.filter;

import com.tle.core.guice.Bind;
import com.tle.web.dispatcher.AbstractWebFilter;
import com.tle.web.dispatcher.FilterResult;
import java.io.IOException;
import java.util.Collection;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;

@Bind
public class SameSiteFilter extends AbstractWebFilter {

  @Override
  public FilterResult filterRequest(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    if (request.isSecure()) {
      Collection<String> headers = response.getHeaders(HttpHeaders.SET_COOKIE);
      boolean firstHeader = true;
      for (String header : headers) {
        if (firstHeader) {
          response.setHeader(
              HttpHeaders.SET_COOKIE, String.format("%s; %s", header, "SameSite=None"));
          firstHeader = false;
          continue;
        }
        response.addHeader(
            HttpHeaders.SET_COOKIE, String.format("%s; %s", header, "SameSite=None"));
      }
    }
    return FilterResult.FILTER_CONTINUE;
  }
}
