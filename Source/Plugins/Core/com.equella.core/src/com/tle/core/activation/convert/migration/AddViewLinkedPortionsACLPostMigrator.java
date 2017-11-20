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

package com.tle.core.activation.convert.migration;

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
import com.tle.core.institution.convert.PostReadMigrator;
import com.tle.core.security.convert.AclConverter.AclPostReadMigratorParams;

@Bind
@Singleton
public class AddViewLinkedPortionsACLPostMigrator implements PostReadMigrator<AclPostReadMigratorParams>
{
	@Inject
	private AccessExpressionDao accessExpressionDao;

	@Override
	public void migrate(AclPostReadMigratorParams list) throws IOException
	{
		final AccessExpression everyone = accessExpressionDao
			.retrieveOrCreate(SecurityConstants.getRecipient(Recipient.EVERYONE));

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
