package com.tle.core.activation.migration;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.beans.security.AccessEntry;
import com.tle.beans.security.AccessExpression;
import com.tle.common.security.SecurityConstants;
import com.tle.common.security.SecurityConstants.Recipient;
import com.tle.core.activation.ActivationConstants;
import com.tle.core.dao.AccessExpressionDao;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.AclConverter.AclPostReadMigratorParams;
import com.tle.core.institution.migration.PostReadMigrator;

@Bind
@Singleton
public class AddViewLinkedPortionsACLPostMigrator implements PostReadMigrator<AclPostReadMigratorParams>
{
	@Inject
	private AccessExpressionDao accessExpressionDao;

	@Override
	public void migrate(AclPostReadMigratorParams list) throws IOException
	{
		final AccessExpression everyone = accessExpressionDao.retrieveOrCreate(SecurityConstants
			.getRecipient(Recipient.EVERYONE));

		AccessEntry newEntry = new AccessEntry();
		newEntry.setGrantRevoke(SecurityConstants.GRANT);
		newEntry.setPrivilege(ActivationConstants.VIEW_LINKED_PORTIONS);
		newEntry.setTargetObject(SecurityConstants.TARGET_EVERYTHING);
		newEntry.setAclPriority(SecurityConstants.PRIORITY_ALL_COLLECTIONS);
		newEntry.setAclOrder(0);
		newEntry.setExpression(everyone);
		newEntry.generateAggregateOrdering();

		list.addAdditionalEntry(newEntry);
	}

}
