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

package com.tle.common.recipientselector.formatter;

import com.tle.common.i18n.CurrentLocale;
import com.tle.common.security.SecurityConstants;
import com.tle.common.security.expressions.ConvertToInfix;
import com.tle.common.usermanagement.util.UserBeanUtils;
import com.tle.common.usermanagement.util.UserDirectoryEntityResolver;

public class ExpressionFormatter extends ConvertToInfix {
  private final UserDirectoryEntityResolver userDirectoryResolver;

  public ExpressionFormatter(UserDirectoryEntityResolver userDirectoryResolver) {
    this.userDirectoryResolver = userDirectoryResolver;
  }

  @Override
  protected String processOperand(String token) {
    String value = SecurityConstants.getRecipientValue(token);
    return switch (SecurityConstants.getRecipientType(token)) {
      case EVERYONE -> CurrentLocale.get("com.tle.admin.recipients.expressionformatter.everyone");
      case OWNER -> CurrentLocale.get("com.tle.admin.recipients.expressionformatter.owner");
      case USER -> UserBeanUtils.getUser(userDirectoryResolver, value).getName();
      case GROUP -> UserBeanUtils.getGroup(userDirectoryResolver, value).getName();
      case ROLE -> UserBeanUtils.getRole(userDirectoryResolver, value).getName();
      case IP_ADDRESS ->
          CurrentLocale.get("com.tle.admin.recipients.expressionformatter.from", value);
      case HTTP_REFERRER ->
          CurrentLocale.get("com.tle.admin.recipients.expressionformatter.referred", value);
      case SHARE_PASS ->
          CurrentLocale.get("com.tle.admin.recipients.expressionformatter.shared", value);
      case TOKEN_SECRET_ID ->
          CurrentLocale.get("com.tle.admin.recipients.expressionformatter.tokenId", value);
      default -> throw new IllegalStateException("Unknown recipient type for token: " + token);
    };
  }
}
