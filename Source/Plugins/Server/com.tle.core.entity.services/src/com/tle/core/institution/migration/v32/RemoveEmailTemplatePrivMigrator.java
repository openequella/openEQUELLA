package com.tle.core.institution.migration.v32;

import java.io.IOException;
import java.util.Iterator;

import javax.inject.Singleton;

import com.tle.beans.security.AccessEntry;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.AclConverter.AclPostReadMigratorParams;
import com.tle.core.institution.migration.PostReadMigrator;

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
			if( entry.getPrivilege().endsWith("EMAIL_TEMPLATE") ) //$NON-NLS-1$
			{
				entries.remove();
			}
		}
	}
}
