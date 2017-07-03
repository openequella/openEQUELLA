package com.tle.core.security.convert.migration.v52;

import java.io.IOException;
import java.util.Iterator;

import javax.inject.Singleton;

import com.tle.beans.security.AccessEntry;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.PostReadMigrator;
import com.tle.core.security.convert.AclConverter.AclPostReadMigratorParams;

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
