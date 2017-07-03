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

package com.tle.core.lti.consumers.migration;

import java.io.IOException;
import java.util.List;

import com.google.common.collect.Lists;
import com.tle.beans.security.AccessEntry;
import com.tle.beans.security.AccessExpression;
import com.tle.common.security.SecurityConstants;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.PostReadMigrator;
import com.tle.core.security.convert.AclConverter.AclPostReadMigratorParams;

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
