package com.tle.cal.migration;

import java.io.IOException;

import javax.inject.Singleton;

import com.tle.beans.security.AccessEntry;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.AclConverter.AclPostReadMigratorParams;
import com.tle.core.institution.migration.PostReadMigrator;

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
