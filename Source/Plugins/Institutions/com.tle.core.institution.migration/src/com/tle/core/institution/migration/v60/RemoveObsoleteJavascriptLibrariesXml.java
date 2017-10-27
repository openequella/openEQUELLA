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

package com.tle.core.institution.migration.v60;

import java.util.List;

import org.w3c.dom.Node;

import com.dytech.devlib.PropBagEx;
import com.google.inject.Singleton;
import com.tle.common.filesystem.handle.SubTemporaryFile;
import com.tle.common.filesystem.handle.TemporaryFileHandle;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.ConverterParams;
import com.tle.core.institution.convert.InstitutionInfo;
import com.tle.core.institution.convert.XmlMigrator;
import com.tle.core.xml.XmlDocument;
import com.tle.core.xml.XmlDocument.NodeListIterable.NodeListIterator;

@Bind
@Singleton
@SuppressWarnings("nls")
public class RemoveObsoleteJavascriptLibrariesXml extends XmlMigrator
{
	private static final String SCRIPT_CONTROL_XPATH = "//com.dytech.edge.wizard.beans.control.CustomControl";

	@Override
	public void execute(TemporaryFileHandle staging, InstitutionInfo instInfo, ConverterParams params)
	{
		final SubTemporaryFile itemDefFolder = new SubTemporaryFile(staging, "itemdefinition");
		final List<String> entries = xmlHelper.getXmlFileList(itemDefFolder);

		for( String collection : entries )
		{
			boolean modified = false;
			PropBagEx itemDef = xmlHelper.readToPropBagEx(itemDefFolder, collection);
			PropBagEx wizard = itemDef.getSubtree("slow/wizard");

			if( wizard != null )
			{
				XmlDocument wizardXml = new XmlDocument(wizard.toString());
				NodeListIterator it = wizardXml.nodeList(SCRIPT_CONTROL_XPATH).iterator();
				while( it.hasNext() )
				{
					Node currentNode = it.next();
					modified |= RemoveObsoleteJavascriptLibraries.deleteLibraries(currentNode, wizardXml);
					it.remove();
				}
				if( modified )
				{
					String newWizard = wizardXml.toString();
					itemDef.deleteSubtree(wizard);
					itemDef.append("slow", new PropBagEx(newWizard));
					xmlHelper.writeFromPropBagEx(staging, collection, itemDef);
				}
			}
		}
	}
}
