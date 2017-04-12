/**
 * 
 */
package com.tle.core.institution.migration.v52;

import java.util.Iterator;

import javax.inject.Singleton;

import com.tle.beans.security.AccessEntry;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.AclConverter.AclPostReadMigratorParams;
import com.tle.core.institution.migration.PostReadMigrator;

/**
 * @author larry Note we are taking advantage of this class to also remove the
 *         obsolete loginnotice and freeetext privileges.
 */
@Bind
@Singleton
public class MergeOneClickSubmitAndVersionSelectionAclXmlMigrator
	implements
		PostReadMigrator<AclPostReadMigratorParams>
{
	@Override
	public void migrate(AclPostReadMigratorParams list)
	{
		Iterator<AccessEntry> entries = list.iterator();
		while( entries.hasNext() )
		{
			AccessEntry entry = entries.next();
			String target = entry.getTargetObject();
			if( target.equals("C:loginnotice") || target.equals("C:oneClickSubmit") || target.equals("C:freetext") )
			{
				entries.remove();
			}
			else if( target.equals("C:versionSelection") )
			{
				entry.setTargetObject("C:selectionSessions");
			}
		}
	}
}
