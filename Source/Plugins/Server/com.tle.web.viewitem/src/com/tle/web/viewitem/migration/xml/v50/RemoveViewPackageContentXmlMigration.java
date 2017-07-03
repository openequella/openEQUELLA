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

package com.tle.web.viewitem.migration.xml.v50;

import java.util.Iterator;

import javax.inject.Singleton;

import com.dytech.devlib.PropBagEx;
import com.tle.common.filesystem.handle.SubTemporaryFile;
import com.tle.common.filesystem.handle.TemporaryFileHandle;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.ConverterParams;
import com.tle.core.institution.convert.InstitutionInfo;
import com.tle.core.institution.convert.XmlMigrator;

@Bind
@Singleton
public class RemoveViewPackageContentXmlMigration extends XmlMigrator
{
	/**
	 * TODO: it seems a lot of overhead to be reading in these XML files
	 * multiple times for the same entity type... e.g. see
	 * DisplayTemplateLocationXmlMigrator which also works on ItemDefinitions
	 * The entity post read migrator should probably have an additional
	 * parameter for the entity type
	 */
	@SuppressWarnings("nls")
	@Override
	public void execute(TemporaryFileHandle staging, InstitutionInfo instInfo, ConverterParams params)
	{
		final SubTemporaryFile itemDefFolder = new SubTemporaryFile(staging, "itemdefinition");
		for( String entry : xmlHelper.getXmlFileList(itemDefFolder) )
		{
			PropBagEx itemDef = xmlHelper.readToPropBagEx(itemDefFolder, entry);

			PropBagEx summarySections = itemDef.getSubtree("slow/itemSummarySections");

			Iterator<PropBagEx> iter = summarySections
				.iterator("configList/com.tle.beans.entity.itemdef.SummarySectionsConfig");
			while( iter.hasNext() )
			{
				if( iter.next().getNode("value").equals("viewContentSection") )
				{
					iter.remove();
				}
			}

			xmlHelper.writeFromPropBagEx(itemDefFolder, entry, itemDef);
		}
	}
}
