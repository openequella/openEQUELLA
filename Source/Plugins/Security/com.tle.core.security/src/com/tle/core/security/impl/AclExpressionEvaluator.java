package com.tle.core.security.impl;

import java.util.Objects;

import com.dytech.edge.common.IpAddressUtils;
import com.dytech.edge.common.IpAddressUtils.Matcher;
import com.tle.common.security.SecurityConstants;
import com.tle.common.security.expressions.EvaluateExpression;
import com.tle.common.usermanagement.user.UserState;

/**
 * @author Nicholas Read
 */
@SuppressWarnings("nls")
public class AclExpressionEvaluator extends EvaluateExpression
{
	private Matcher ipAddressMatcher;
	private UserState userState;
	private boolean isOwner;
	private boolean enableIpReferAcl;

	public AclExpressionEvaluator()
	{
		super();
	}

	public boolean evaluate(String expression, UserState userState, boolean isOwner)
	{
		return this.evaluate(expression, userState, isOwner, true);
	}

	public boolean evaluate(String expression, UserState userState, boolean isOwner, boolean enableIpReferAcl)
	{
		this.userState = userState;
		this.isOwner = isOwner;
		this.enableIpReferAcl = enableIpReferAcl;

		return evaluate(expression);
	}

	@Override
	protected Boolean processOperand(String token)
	{
		String value = SecurityConstants.getRecipientValue(token);
		switch( SecurityConstants.getRecipientType(token) )
		{
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

	private boolean checkIpAddressRange(String cidrAddress)
	{
		String userIpAddress = userState.getIpAddress();
		if( userIpAddress != null )
		{
			// IPv6, no current implementation for this
			if( userIpAddress.contains(":") )
			{
				return false;
			}

			if( ipAddressMatcher == null )
			{
				ipAddressMatcher = IpAddressUtils.matchRangesAgainstIpAddress(userIpAddress);
			}
			return ipAddressMatcher.matches(cidrAddress);
		}
		return false;
	}

	private boolean checkReferrer(final String token)
	{
		String referrer = userState.getHostReferrer();
		if( referrer == null )
		{
			return false;
		}

		if( token.charAt(0) == '*' )
		{
			String t = token.substring(1);

			if( t.endsWith("*") )
			{
				t = t.substring(0, t.length() - 1);
			}

			return referrer.toLowerCase().contains(t.toLowerCase());
		}

		return referrer.equalsIgnoreCase(token);
	}
}
