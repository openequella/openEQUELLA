package com.tle.core.lti.consumers.migration;

import java.io.IOException;
import java.util.List;

import com.google.common.collect.Lists;
import com.tle.beans.security.AccessEntry;
import com.tle.beans.security.AccessExpression;
import com.tle.common.security.SecurityConstants;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.AclConverter.AclPostReadMigratorParams;
import com.tle.core.institution.migration.PostReadMigrator;

@Bind
public class ConvertOauthACLsPostMigrator implements PostReadMigrator<AclPostReadMigratorParams>
{

	@Override
	public void migrate(AclPostReadMigratorParams list) throws IOException
	{
		final String OAUTH_CLIENT = "OAUTH_CLIENT";
		List<AccessEntry> newEntries = Lists.newArrayList();

		for( AccessEntry accessEntry : list )
		{
			if( accessEntry.getPrivilege().contains(OAUTH_CLIENT) )
			{
				AccessEntry newEntry = new AccessEntry();

				AccessExpression newExpression = list.getExpressionsFromXml().get(accessEntry.getExpression().getId());
				newEntry.setExpression(newExpression);
				newEntry.setTargetObject(accessEntry.getTargetObject());
				newEntry.setAclOrder(accessEntry.getAclOrder());
				newEntry.setGrantRevoke(accessEntry.isGrantRevoke());

				newEntry.setPrivilege(accessEntry.getPrivilege().replace(OAUTH_CLIENT, "LTI_CONSUMER"));

				int currPriority = accessEntry.getAclPriority();
				int newPriority = Math.abs(currPriority) == SecurityConstants.PRIORITY_INSTITUTION ? currPriority
					: currPriority < 0 ? (currPriority - 4) : (currPriority + 4);
				newEntry.setAclPriority(newPriority);
				newEntry.generateAggregateOrdering();

				newEntries.add(newEntry);
			}
		}

		for( AccessEntry newEntry : newEntries )
		{
			list.addAdditionalEntry(newEntry);
		}

	}
}
