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

package com.tle.core.wizard.institution.migration.v41;

import java.util.List;

import javax.inject.Singleton;

import com.dytech.devlib.PropBagEx;
import com.tle.common.Check;
import com.tle.common.filesystem.handle.SubTemporaryFile;
import com.tle.common.filesystem.handle.TemporaryFileHandle;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.ConverterParams;
import com.tle.core.institution.convert.InstitutionInfo;
import com.tle.core.institution.convert.XmlMigrator;
import com.tle.core.institution.convert.DefaultMessageCallback;

/**
 * @author aholland
 */
@Bind
@Singleton
@SuppressWarnings("nls")
public class WizardLayoutXmlMigrator extends XmlMigrator
{
	@Override
	public void execute(TemporaryFileHandle staging, InstitutionInfo instInfo, ConverterParams params)
	{
		final SubTemporaryFile itemdefFolder = new SubTemporaryFile(staging, "itemdefinition");
		final List<String> entries = xmlHelper.getXmlFileList(itemdefFolder);

		DefaultMessageCallback message = new DefaultMessageCallback(
			"com.tle.core.wizard.institution.migration.v41.layoutmigrator.progressmessage");
		params.setMessageCallback(message);
		message.setType(CurrentLocale.get("com.tle.core.wizard.wizard"));
		message.setTotal(entries.size());

		for( String collection : entries )
		{
			PropBagEx itemDef = xmlHelper.readToPropBagEx(itemdefFolder, collection);

			PropBagEx wizard = itemDef.getSubtree("slow/wizard");
			if( wizard != null )
			{
				// The latest version of the code removes the layout, so we
				// won't even bother updating the XML.

				// String layout = wizard.getNode("layout");
				// if( Check.isEmpty(layout) )
				// {
				// wizard.setNode("layout",
				// WizardConstants.DEFAULT_WIZARD_LAYOUT);
				// }

				// Not strictly necessary...
				String allowNonSequentialNavigation = wizard.getNode("allowNonSequentialNavigation");
				if( Check.isEmpty(allowNonSequentialNavigation) )
				{
					wizard.setNode("allowNonSequentialNavigation", "false");
				}
			}
			xmlHelper.writeFromPropBagEx(itemdefFolder, collection, itemDef);
			message.incrementCurrent();
		}
	}
}
