package com.tle.core.institution.migration;

import java.io.IOException;

import javax.inject.Singleton;

import com.tle.beans.security.AccessEntry;
import com.tle.common.security.SecurityConstants;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.AclConverter.AclPostReadMigratorParams;

/**
 * We need to introduce new ACL priorities for non-specific ACL targets, like
 * "All Collections" and "All Schemas". We do not need to do this for ACLs with
 * an Institution priority.
 * 
 * @author Nicholas Read
 */
@Bind
@Singleton
public class AclPriorityChange implements PostReadMigrator<AclPostReadMigratorParams>
{
	@Override
	public void migrate(AclPostReadMigratorParams entries) throws IOException
	{
		for( AccessEntry entry : entries )
		{
			if( entry.getTargetObject().equals(SecurityConstants.TARGET_EVERYTHING) )
			{
				int priority = entry.getAclPriority();
				if( Math.abs(priority) != SecurityConstants.PRIORITY_INSTITUTION )
				{
					if( priority > 0 )
					{
						priority += 25;
					}
					else
					{
						priority -= 25;
					}
					entry.setAclPriority(priority);

					entry.generateAggregateOrdering();
				}
			}
		}
	}
}
