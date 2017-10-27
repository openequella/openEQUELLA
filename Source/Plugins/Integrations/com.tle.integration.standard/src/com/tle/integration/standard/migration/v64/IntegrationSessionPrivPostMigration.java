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

package com.tle.integration.standard.migration.v64;

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
public class IntegrationSessionPrivPostMigration implements PostReadMigrator<AclPostReadMigratorParams>
{
	@Inject
	private AccessExpressionDao accessExpressionDao;
	@Inject
	private AclDao aclDao;

	@Override
	public void migrate(AclPostReadMigratorParams obj) throws IOException
	{
		final AccessExpression everyone = accessExpressionDao
			.retrieveOrCreate(SecurityConstants.getRecipient(Recipient.EVERYONE));

		grantPrivilege("INTEGRATION_SELECTION_SESSION", everyone, SecurityConstants.TARGET_EVERYTHING,
			CurrentInstitution.get());
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
