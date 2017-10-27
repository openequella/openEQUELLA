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

package com.tle.beans.usermanagement.standard.wrapper;

import java.util.ArrayList;
import java.util.List;

import com.tle.common.settings.annotation.Property;
import com.tle.common.settings.annotation.PropertyDataList;

public class SharedSecretSettings extends AbstractSharedSecretSettings<SharedSecretSettings.SharedSecretValue>
{
	private static final long serialVersionUID = -1499107242578596538L;

	@PropertyDataList(key = "security.shared.secrets", type = SharedSecretValue.class)
	private List<SharedSecretValue> sharedSecrets = new ArrayList<SharedSecretValue>();

	public static class SharedSecretValue extends AbstractSharedSecretSettings.AbstractSharedSecretValue
	{
		private static final long serialVersionUID = -1384092013693189870L;

		@Property(key = "prefix")
		private String prefix = ""; //$NON-NLS-1$

		@Property(key = "postfix")
		private String postfix = ""; //$NON-NLS-1$

		@Property(key = "autoCreate")
		private boolean autoCreate = false;

		@Property(key = "ignoreFailure")
		private boolean ignoreNonExistantUser = false;

		public String getPostfix()
		{
			return postfix;
		}

		public void setPostfix(String postfix)
		{
			this.postfix = postfix;
		}

		public String getPrefix()
		{
			return prefix;
		}

		public void setPrefix(String prefix)
		{
			this.prefix = prefix;
		}

		public boolean isAutoCreate()
		{
			return autoCreate;
		}

		public void setAutoCreate(boolean autoCreate)
		{
			this.autoCreate = autoCreate;
		}

		public boolean isIgnoreNonExistantUser()
		{
			return ignoreNonExistantUser;
		}

		public void setIgnoreNonExistantUser(boolean ignoreNonExistantUser)
		{
			this.ignoreNonExistantUser = ignoreNonExistantUser;
		}
	}

	@Property(key = "wrapper.shared.enabled")
	private boolean enabled;

	@Override
	public boolean isEnabled()
	{
		return enabled;
	}

	@Override
	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
	}

	@Override
	public List<SharedSecretValue> getSharedSecrets()
	{
		if( sharedSecrets == null )
		{
			sharedSecrets = new ArrayList<SharedSecretValue>();
		}
		return sharedSecrets;
	}

	@Override
	public void setSharedSecrets(List<SharedSecretValue> sharedSecrets)
	{
		this.sharedSecrets = sharedSecrets;
	}
}
