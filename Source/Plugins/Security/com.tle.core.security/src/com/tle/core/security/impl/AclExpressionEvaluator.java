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

package com.tle.core.security.impl;

import com.dytech.edge.common.IpAddressUtils;
import com.dytech.edge.common.IpAddressUtils.Matcher;
import com.tle.common.security.SecurityConstants;
import com.tle.common.security.expressions.EvaluateExpression;
import com.tle.common.usermanagement.user.UserState;
import java.util.Objects;

@SuppressWarnings("nls")
public class AclExpressionEvaluator extends EvaluateExpression {
  private Matcher ipAddressMatcher;
  private UserState userState;
  private boolean isOwner;
  private boolean enableIpReferAcl;

  public AclExpressionEvaluator() {
    super();
  }

  public boolean evaluate(String expression, UserState userState, boolean isOwner) {
    return this.evaluate(expression, userState, isOwner, true);
  }

  public boolean evaluate(
      String expression, UserState userState, boolean isOwner, boolean enableIpReferAcl) {
    this.userState = userState;
    this.isOwner = isOwner;
    this.enableIpReferAcl = enableIpReferAcl;

    return evaluate(expression);
  }

  @Override
  protected Boolean processOperand(String token) {
    String value = SecurityConstants.getRecipientValue(token);
    switch (SecurityConstants.getRecipientType(token)) {
      case EVERYONE:
        return true;
      case OWNER:
        return !userState.isGuest() && isOwner;
      case USER:
        return !userState.isGuest() && userState.getUserBean().getUniqueID().equals(value);
      case GROUP:
        return userState.getUsersGroups().contains(value);
      case ROLE:
        return userState.getUsersRoles().contains(value);
      case IP_ADDRESS:
        return enableIpReferAcl ? checkIpAddressRange(value) : true;
      case HTTP_REFERRER:
        return enableIpReferAcl ? checkReferrer(value) : true;
      case SHARE_PASS:
        return Objects.equals(userState.getSharePassEmail(), value);
      case TOKEN_SECRET_ID:
        return Objects.equals(userState.getTokenSecretId(), value);
      default:
        throw new IllegalStateException();
    }
  }

  private boolean checkIpAddressRange(String cidrAddress) {
    String userIpAddress = userState.getIpAddress();
    if (userIpAddress != null) {
      // IPv6, no current implementation for this
      if (userIpAddress.contains(":")) {
        return false;
      }

      if (ipAddressMatcher == null) {
        ipAddressMatcher = IpAddressUtils.matchRangesAgainstIpAddress(userIpAddress);
      }
      return ipAddressMatcher.matches(cidrAddress);
    }
    return false;
  }

  private boolean checkReferrer(final String token) {
    String referrer = userState.getHostReferrer();
    if (referrer == null) {
      return false;
    }

    if (token.charAt(0) == '*') {
      String t = token.substring(1);

      if (t.endsWith("*")) {
        t = t.substring(0, t.length() - 1);
      }

      return referrer.toLowerCase().contains(t.toLowerCase());
    }

    return referrer.equalsIgnoreCase(token);
  }
}
