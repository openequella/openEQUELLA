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

package com.tle.core.institution.migration.v40;

import java.util.List;

import javax.inject.Singleton;

import com.dytech.devlib.PropBagEx;
import com.tle.common.Check;
import com.tle.common.filesystem.handle.SubTemporaryFile;
import com.tle.common.filesystem.handle.TemporaryFileHandle;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.ConverterParams;
import com.tle.core.institution.convert.InstitutionInfo;
import com.tle.core.institution.convert.XmlMigrator;

@Bind
@Singleton
@SuppressWarnings("nls")
public class MigrateSystemField extends XmlMigrator
{
	@Override
	public void execute(TemporaryFileHandle staging, InstitutionInfo instInfo, ConverterParams params)
	{
		final SubTemporaryFile folder = new SubTemporaryFile(staging, "itemdefinition");
		final List<String> entries = xmlHelper.getXmlFileList(folder);
		for( String entry : entries )
		{
			PropBagEx itemDef = xmlHelper.readToPropBagEx(folder, entry);
			String isSystem = itemDef.getNode("system");
			if( !Check.isEmpty(isSystem) )
			{
				itemDef.deleteNode("system");
				itemDef.createNode("systemType", isSystem);
				xmlHelper.writeFromPropBagEx(folder, entry, itemDef);
			}
		}
	}

}
