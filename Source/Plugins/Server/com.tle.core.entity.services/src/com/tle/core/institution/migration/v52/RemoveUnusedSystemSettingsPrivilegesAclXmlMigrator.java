package com.tle.core.institution.migration.v52;

import java.io.IOException;
import java.util.Iterator;

import javax.inject.Singleton;

import com.tle.beans.security.AccessEntry;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.AclConverter.AclPostReadMigratorParams;
import com.tle.core.institution.migration.PostReadMigrator;

/**
 * @author Aaron
 */
@Bind
@Singleton
public class RemoveUnusedSystemSettingsPrivilegesAclXmlMigrator implements PostReadMigrator<AclPostReadMigratorParams>
{
	@Override
	public void migrate(AclPostReadMigratorParams list) throws IOException
	{
		Iterator<AccessEntry> entries = list.iterator();
		while( entries.hasNext() )
		{
			AccessEntry entry = entries.next();
			String target = entry.getTargetObject();
			if( target.equals("C:attachmentFileTypes") || target.equals("C:proxy") || target.equals("C:sif") )
			{
				entries.remove();
			}
		}
	}
}
