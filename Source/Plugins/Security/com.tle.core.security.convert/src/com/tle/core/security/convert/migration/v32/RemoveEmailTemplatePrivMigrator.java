package com.tle.core.security.convert.migration.v32;

import java.io.IOException;
import java.util.Iterator;

import javax.inject.Singleton;

import com.tle.beans.security.AccessEntry;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.PostReadMigrator;
import com.tle.core.security.convert.AclConverter.AclPostReadMigratorParams;

/**
 * @author aholland
 */
@Bind
@Singleton
public class RemoveEmailTemplatePrivMigrator implements PostReadMigrator<AclPostReadMigratorParams>
{
	@Override
	public void migrate(AclPostReadMigratorParams list) throws IOException
	{
		Iterator<AccessEntry> entries = list.iterator();
		while( entries.hasNext() )
		{
			AccessEntry entry = entries.next();
			if( entry.getPrivilege().endsWith("EMAIL_TEMPLATE") )
			{
				entries.remove();
			}
		}
	}
}
