package com.tle.core.security.convert.migration.v64;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.beans.Institution;
import com.tle.beans.security.AccessEntry;
import com.tle.beans.security.AccessExpression;
import com.tle.common.institution.CurrentInstitution;
import com.tle.common.security.SecurityConstants;
import com.tle.common.security.SecurityConstants.Recipient;
import com.tle.core.dao.AccessExpressionDao;
import com.tle.core.dao.AclDao;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.PostReadMigrator;
import com.tle.core.security.convert.AclConverter.AclPostReadMigratorParams;

@Bind
@Singleton
public class NewPagesACLsPostMigration implements PostReadMigrator<AclPostReadMigratorParams>
{
	@Inject
	private AccessExpressionDao accessExpressionDao;
	@Inject
	private AclDao aclDao;

	@Override
	public void migrate(AclPostReadMigratorParams obj) throws IOException
	{
		final AccessExpression loggedInUser = accessExpressionDao
			.retrieveOrCreate(SecurityConstants.getRecipient(Recipient.ROLE, SecurityConstants.LOGGED_IN_USER_ROLE_ID));

		grantPrivilege("SEARCH_PAGE", loggedInUser, SecurityConstants.TARGET_EVERYTHING, CurrentInstitution.get());
		grantPrivilege("DASHBOARD_PAGE", loggedInUser, SecurityConstants.TARGET_EVERYTHING, CurrentInstitution.get());
		grantPrivilege("HIERARCHY_PAGE", loggedInUser, SecurityConstants.TARGET_EVERYTHING, CurrentInstitution.get());

	}

	private void grantPrivilege(String privilege, AccessExpression expression, String target, Institution institution)
	{
		addEntry(privilege, SecurityConstants.GRANT, expression, target, institution);
	}

	private void addEntry(String privilege, char grantRevoke, AccessExpression expression, String target,
		Institution institution)
	{
		AccessEntry newEntry = new AccessEntry();
		newEntry.setGrantRevoke(grantRevoke);
		newEntry.setPrivilege(privilege);
		newEntry.setTargetObject(target);
		newEntry.setAclPriority(-SecurityConstants.PRIORITY_INSTITUTION);
		newEntry.setAclOrder(0);
		newEntry.setExpression(expression);
		newEntry.setInstitution(institution);

		aclDao.save(newEntry);
		aclDao.flush();
		aclDao.clear();
	}

}
