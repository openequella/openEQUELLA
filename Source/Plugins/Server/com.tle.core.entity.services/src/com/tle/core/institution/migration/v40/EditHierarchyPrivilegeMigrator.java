package com.tle.core.institution.migration.v40;

import java.io.IOException;

import javax.inject.Singleton;

import com.tle.beans.security.AccessEntry;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.AclConverter.AclPostReadMigratorParams;
import com.tle.core.institution.migration.PostReadMigrator;

@Bind
@Singleton
public class EditHierarchyPrivilegeMigrator implements PostReadMigrator<AclPostReadMigratorParams>
{
	@Override
	public void migrate(AclPostReadMigratorParams list) throws IOException
	{
		for( AccessEntry entry : list )
		{
			if( entry.getPrivilege().equals("EDIT_HIERARCHY") )
			{
				entry.setPrivilege("EDIT_HIERARCHY_TOPIC");
			}
		}
	}
}
