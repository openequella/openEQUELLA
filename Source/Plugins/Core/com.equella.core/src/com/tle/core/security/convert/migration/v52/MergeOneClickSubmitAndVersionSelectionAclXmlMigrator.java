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

package com.tle.core.security.convert.migration.v52;

import java.util.Iterator;

import javax.inject.Singleton;

import com.tle.beans.security.AccessEntry;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.PostReadMigrator;
import com.tle.core.security.convert.AclConverter.AclPostReadMigratorParams;

/**
 * @author larry Note we are taking advantage of this class to also remove the
 *         obsolete loginnotice and freeetext privileges.
 */
@Bind
@Singleton
public class MergeOneClickSubmitAndVersionSelectionAclXmlMigrator implements PostReadMigrator<AclPostReadMigratorParams>
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
