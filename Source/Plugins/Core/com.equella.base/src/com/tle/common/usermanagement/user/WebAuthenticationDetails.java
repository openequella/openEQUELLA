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

package com.tle.common.usermanagement.user;

import java.io.Serializable;

public class WebAuthenticationDetails implements Serializable {
  private static final long serialVersionUID = 1L;
  private final String referrer;
  private final String ipAddress;
  private final String hostAddress;

  public WebAuthenticationDetails(UserState userState) {
    this(userState.getHostReferrer(), userState.getIpAddress(), userState.getHostAddress());
  }

  public WebAuthenticationDetails(String referrer, String ipAddress, String hostAddress) {
    this.referrer = referrer;
    this.ipAddress = ipAddress;
    this.hostAddress = hostAddress;
  }

  public String getIpAddress() {
    return ipAddress;
  }

  public String getReferrer() {
    return referrer;
  }

  public String getHostAddress() {
    return hostAddress;
  }
}
