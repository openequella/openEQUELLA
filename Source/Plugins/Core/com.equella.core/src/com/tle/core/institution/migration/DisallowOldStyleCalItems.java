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

package com.tle.core.institution.migration;

import java.util.Objects;

import javax.inject.Singleton;

import com.dytech.devlib.PropBagEx;
import com.tle.common.filesystem.handle.SubTemporaryFile;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.AbstractItemXmlMigrator;
import com.tle.core.institution.convert.ConverterParams;

@Bind
@Singleton
@SuppressWarnings("nls")
public class DisallowOldStyleCalItems extends AbstractItemXmlMigrator
{
	@Override
	public boolean migrate(ConverterParams params, PropBagEx xml, SubTemporaryFile file, String filename)
		throws Exception
	{
		if( Objects.equals("Cradle", params.getBranchString()) )
		{
			if( xml.nodeExists("cal") )
			{
				throw new Exception("CAL item can not be migrated: items/" + filename);
			}
		}
		else if( Objects.equals("Cradle2", params.getBranchString()) )
		{
			xml.deleteNode("requests");
			for( PropBagEx activate : xml.iterateAll("activateRequests/*") )
			{
				activate.deleteNode("courseOld");
			}
			return true;
		}
		return false;
	}
}
