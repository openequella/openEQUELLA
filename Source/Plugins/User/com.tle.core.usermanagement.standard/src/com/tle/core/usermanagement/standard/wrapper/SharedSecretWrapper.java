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

package com.tle.core.usermanagement.standard.wrapper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.tle.beans.usermanagement.standard.wrapper.SharedSecretSettings.SharedSecretValue;
import com.tle.common.usermanagement.util.TokenSecurity.Token;
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
