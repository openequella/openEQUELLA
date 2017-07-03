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

package com.tle.common.recipientselector;

import com.tle.common.i18n.CurrentLocale;
import com.tle.common.security.SecurityConstants;
import com.tle.common.security.expressions.ConvertToInfix;
import com.tle.common.usermanagement.util.UserBeanUtils;
import com.tle.core.remoting.RemoteUserService;

/**
 * @author Nicholas Read
 */
public class ExpressionFormatter extends ConvertToInfix
{
	private final RemoteUserService userService;

	public ExpressionFormatter(RemoteUserService userService)
	{
		this.userService = userService;
	}

	@Override
	@SuppressWarnings("nls")
	protected String processOperand(String token)
	{
		String value = SecurityConstants.getRecipientValue(token);
		switch( SecurityConstants.getRecipientType(token) )
		{
			case EVERYONE:
				return CurrentLocale.get("com.tle.admin.recipients.expressionformatter.everyone");
			case OWNER:
				return CurrentLocale.get("com.tle.admin.recipients.expressionformatter.owner");
			case USER:
				return UserBeanUtils.getUser(userService, value).getName();
			case GROUP:
				return UserBeanUtils.getGroup(userService, value).getName();
			case ROLE:
				return UserBeanUtils.getRole(userService, value).getName();
			case IP_ADDRESS:
				return CurrentLocale.get("com.tle.admin.recipients.expressionformatter.from", value);
			case HTTP_REFERRER:
				return CurrentLocale.get("com.tle.admin.recipients.expressionformatter.referred", value);
			case SHARE_PASS:
				return CurrentLocale.get("com.tle.admin.recipients.expressionformatter.shared", value);
			case TOKEN_SECRET_ID:
				return CurrentLocale.get("com.tle.admin.recipients.expressionformatter.tokenId", value);
			default:
				throw new IllegalStateException(token);
		}
	}
}
