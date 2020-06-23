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

package com.tle.core.connectors.blackboard;

import com.tle.annotation.Nullable;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;

public class BlackboardRestAppContext {
  private static final String STATE_PARAMETER = "state";
  private static final String FIELD_REDIRECT_URI = "redirect_uri";
  private static final String KEY_VALUE_RESPONSE_TYPE_CODE = "response_type=code";
  private static final String FIELD_CLIENT_ID = "client_id";
  private static final String FIELD_SCOPE = "scope";
  private static final String VALUE_READ_WRITE_DELETE = "read write delete";

  private final String _appId;
  private final String _appKey;
  private String _url;

  /**
   * Constructs a BlackboardRestAppContext with the provided application values
   *
   * @param appId The application ID provided by the key tool
   * @param appKey The application key provided by the key tool
   * @param url The url of the Bb instance
   */
  public BlackboardRestAppContext(String appId, String appKey, String url) {
    _appId = appId;
    _appKey = appKey;
    if (url != null && url.endsWith("/")) {
      _url = url.substring(0, url.lastIndexOf("/"));
    } else {
      _url = url;
    }
  }

  public URI createWebUrlForAuthentication(URI redirectUrl, @Nullable String state) {
    try {
      URI uri =
          new URI(
              _url
                  + BlackboardRESTConnectorConstants.AUTHENTICATIONCODE_SERVICE_URI_PATH
                  + "?"
                  + buildAuthenticationCodeUriQueryString(redirectUrl, state));
      return uri;
    } catch (URISyntaxException e) {
      return null;
    }
  }

  private String buildAuthenticationCodeUriQueryString(URI callbackUri, @Nullable String state) {
    String callbackUriString = callbackUri.toString();
    String result = KEY_VALUE_RESPONSE_TYPE_CODE;
    result += "&" + FIELD_REDIRECT_URI + "=" + callbackUriString;
    result += "&" + FIELD_CLIENT_ID + "=" + _appId;
    result += "&" + FIELD_SCOPE + "=" + URLEncoder.encode(VALUE_READ_WRITE_DELETE);
    if (state != null) {
      result += "&" + STATE_PARAMETER + "=" + URLEncoder.encode(state);
    }
    return result;
  }
}
