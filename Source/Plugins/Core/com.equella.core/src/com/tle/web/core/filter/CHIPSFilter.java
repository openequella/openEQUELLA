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

/**
 * As per Google's recommendation for how to prepare for phasing out third party cookies, this
 * filter implements the feature of <a
 * href="https://developers.google.com/privacy-sandbox/3pcd/chips">CHIPS</a> so that all the cookies
 * will have an attribute "Partitioned" added to them when the request was made using a secure
 * channel such as HTTPS.
 */
@Bind
public class CHIPSFilter extends AbstractWebFilter {

  @Override
  public FilterResult filterRequest(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    if (request.isSecure()) {
      Collection<String> headers = response.getHeaders(HttpHeaders.SET_COOKIE);
      for (String header : headers) {
        response.setHeader(HttpHeaders.SET_COOKIE, String.format("%s; %s", header, "Partitioned"));
      }
    }
    return FilterResult.FILTER_CONTINUE;
  }
}
