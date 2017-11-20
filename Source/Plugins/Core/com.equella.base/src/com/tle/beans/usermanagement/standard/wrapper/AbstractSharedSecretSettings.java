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

import com.tle.beans.ump.UserManagementSettings;
import com.tle.common.settings.ConfigurationProperties;
import com.tle.common.settings.annotation.Property;
import com.tle.common.settings.annotation.PropertyList;

public abstract class AbstractSharedSecretSettings<S extends AbstractSharedSecretSettings.AbstractSharedSecretValue>
	extends
		UserManagementSettings
{
	public abstract List<S> getSharedSecrets();

	public abstract void setSharedSecrets(List<S> sharedSecrets);

	public static class AbstractSharedSecretValue implements ConfigurationProperties
	{
		private static final long serialVersionUID = 7008004720184372536L;

		@Property(key = "id")
		private String id = ""; //$NON-NLS-1$

		@Property(key = "secret")
		private String secret = ""; //$NON-NLS-1$

		@Property(key = "expression")
		private String expression = "*"; //$NON-NLS-1$

		@PropertyList(key = "groups")
		private List<String> groups = new ArrayList<String>();

		public String getId()
		{
			return id;
		}

		public void setId(String id)
		{
			this.id = id;
		}

		public String getExpression()
		{
			return expression;
		}

		public void setExpression(String expression)
		{
			this.expression = expression;
		}

		public String getSecret()
		{
			return secret;
		}

		public void setSecret(String secret)
		{
			this.secret = secret;
		}

		public List<String> getGroups()
		{
			return groups;
		}
	}
}
