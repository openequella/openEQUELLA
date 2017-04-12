package com.tle.common.recipientselector;

import com.tle.common.i18n.CurrentLocale;
import com.tle.common.security.SecurityConstants;
import com.tle.common.security.expressions.ConvertToInfix;
import com.tle.common.util.UserBeanUtils;
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
