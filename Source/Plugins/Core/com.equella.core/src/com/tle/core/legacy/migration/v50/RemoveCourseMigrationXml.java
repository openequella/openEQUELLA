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

package com.tle.core.legacy.migration.v50;

import java.util.Iterator;

import javax.inject.Singleton;

import com.dytech.devlib.PropBagEx;
import com.tle.common.filesystem.handle.TemporaryFileHandle;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.ConverterParams;
import com.tle.core.institution.convert.InstitutionInfo;
import com.tle.core.institution.convert.XmlMigrator;

@Bind
@Singleton
@SuppressWarnings("nls")
public class RemoveCourseMigrationXml extends XmlMigrator
{
	@Override
	public void execute(TemporaryFileHandle staging, InstitutionInfo instInfo, ConverterParams params)
	{
		if( fileSystemService.fileExists(staging, "acls/entries.xml") )
		{
			boolean changed = false;
			PropBagEx xml = xmlHelper.readToPropBagEx(staging, "acls/entries.xml");
			for( Iterator<PropBagEx> iter = xml.iterator(); iter.hasNext(); )
			{
				if( iter.next().getNode("privilege").equals("LAUNCH_ACTIVITY_MANAGER") )
				{
					iter.remove();
					changed |= true;
				}
			}

			if( changed )
			{
				xmlHelper.writeFromPropBagEx(staging, "acls/entries.xml", xml);
			}
		}
	}
}