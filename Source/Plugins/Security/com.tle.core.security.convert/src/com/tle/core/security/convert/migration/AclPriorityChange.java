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

package com.tle.core.security.convert.migration;

import java.io.IOException;

import javax.inject.Singleton;

import com.tle.beans.security.AccessEntry;
import com.tle.common.security.SecurityConstants;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.PostReadMigrator;
import com.tle.core.security.convert.AclConverter.AclPostReadMigratorParams;

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
