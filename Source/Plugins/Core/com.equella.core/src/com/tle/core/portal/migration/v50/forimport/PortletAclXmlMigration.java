/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.core.portal.migration.v50.forimport;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.beans.security.AccessEntry;
import com.tle.beans.security.AccessExpression;
import com.tle.common.security.SecurityConstants;
import com.tle.common.security.SecurityConstants.Recipient;
import com.tle.core.dao.AccessExpressionDao;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.PostReadMigrator;
import com.tle.core.security.convert.AclConverter.AclPostReadMigratorParams;

/**
 * @author aholland
 */
@Bind
@Singleton
public class PortletAclXmlMigration implements PostReadMigrator<AclPostReadMigratorParams>
{
	@Inject
	private AccessExpressionDao accessExpressionDao;

	@Override
	@SuppressWarnings("nls")
	public void migrate(AclPostReadMigratorParams list) throws IOException
	{
		final AccessExpression owner = accessExpressionDao
			.retrieveOrCreate(SecurityConstants.getRecipient(Recipient.OWNER));
		final AccessExpression everyone = accessExpressionDao
			.retrieveOrCreate(SecurityConstants.getRecipient(Recipient.EVERYONE));

		grantPrivilege(list, "CREATE_PORTLET", everyone, "*");

		grantPrivilege(list, "VIEW_PORTLET", owner, "*");
		grantPrivilege(list, "EDIT_PORTLET", owner, "*");
		grantPrivilege(list, "DELETE_PORTLET", owner, "*");
	}

	private void grantPrivilege(AclPostReadMigratorParams list, String privilege, AccessExpression expression,
		String target)
	{
		addEntry(list, privilege, SecurityConstants.GRANT, expression, target);
	}

	private void addEntry(AclPostReadMigratorParams list, String privilege, char grantRevoke,
		AccessExpression expression, String target)
	{
		AccessEntry newEntry = new AccessEntry();
		newEntry.setGrantRevoke(grantRevoke);
		newEntry.setPrivilege(privilege);
		newEntry.setTargetObject(target);
		newEntry.setAclPriority(SecurityConstants.PRIORITY_ALL_PORTLETS);
		newEntry.setAclOrder(0);
		newEntry.setExpression(expression);
		newEntry.generateAggregateOrdering();
		list.addAdditionalEntry(newEntry);
	}
}
