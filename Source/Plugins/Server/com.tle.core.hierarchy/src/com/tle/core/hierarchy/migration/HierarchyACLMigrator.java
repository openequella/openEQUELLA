package com.tle.core.hierarchy.migration;

import java.io.IOException;

import javax.inject.Singleton;

import com.tle.beans.security.AccessEntry;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.AclConverter.AclPostReadMigratorParams;
import com.tle.core.institution.migration.PostReadMigrator;

@Bind
@Singleton
public class HierarchyACLMigrator implements PostReadMigrator<AclPostReadMigratorParams>
{
	@Override
	public void migrate(AclPostReadMigratorParams list) throws IOException
	{
		for( AccessEntry accessEntry : list )
		{
			String priv = accessEntry.getPrivilege();
			if( priv.equals("ADD_K_R_TO_HIERARCHY_TOPIC") ) //$NON-NLS-1$
			{
				accessEntry.setPrivilege("MODIFY_KEY_RESOURCE"); //$NON-NLS-1$
			}
		}
	}
}
