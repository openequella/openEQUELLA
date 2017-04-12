/*
 * Created on 15/12/2005
 */
package com.tle.beans.usermanagement.standard.wrapper;

import java.util.ArrayList;
import java.util.List;

import com.tle.beans.ump.UserManagementSettings;
import com.tle.common.property.ConfigurationProperties;
import com.tle.common.property.annotation.Property;
import com.tle.common.property.annotation.PropertyList;

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
