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

package com.tle.cal.migration;

import java.io.IOException;

import javax.inject.Singleton;

import com.tle.beans.security.AccessEntry;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.PostReadMigrator;
import com.tle.core.security.convert.AclConverter.AclPostReadMigratorParams;

@Bind
@Singleton
public class CalPrivMigrator implements PostReadMigrator<AclPostReadMigratorParams>
{
	@Override
	public void migrate(AclPostReadMigratorParams list) throws IOException
	{
		for( AccessEntry accessEntry : list )
		{
			String priv = accessEntry.getPrivilege();
			if( priv.equals("DELETE_ACTIVATION_REQUEST") ) //$NON-NLS-1$
			{
				accessEntry.setPrivilege("DELETE_ACTIVATION_ITEM"); //$NON-NLS-1$
			}
			else if( priv.equals("VIEW_ACTIVATION_REQUEST") ) //$NON-NLS-1$
			{
				accessEntry.setPrivilege("VIEW_ACTIVATION_ITEM"); //$NON-NLS-1$
			}
		}
	}
}
