/*
 * Created on Apr 19, 2005
 */
package com.tle.core.usermanagement.standard.wrapper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.tle.beans.usermanagement.standard.wrapper.SharedSecretSettings.SharedSecretValue;
import com.tle.common.util.TokenSecurity.Token;
import com.tle.core.guice.Bind;

@Bind
public class SharedSecretWrapper extends AbstractSharedSecretWrapper<SharedSecretValue>
{
	static final Log LOGGER = LogFactory.getLog(SharedSecretWrapper.class);

	@Override
	protected String getUsername(SharedSecretValue value, Token token)
	{
		return value.getPrefix() + token.getInsecure() + value.getPostfix();
	}

	@Override
	protected boolean isAutoCreate(SharedSecretValue value)
	{
		return value.isAutoCreate();
	}

	@Override
	protected boolean isIgnoreNonExistantUser(SharedSecretValue value)
	{
		return value.isIgnoreNonExistantUser();
	}

	@Override
	public boolean isAuditable()
	{
		return true;
	}
}
