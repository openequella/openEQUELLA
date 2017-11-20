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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

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
@SuppressWarnings("nls")
public class RemoveAssemblerAndActivityWizardEntitiesFix extends XmlMigrator
{
	public static final Set<String> ASSEM_COLLECTIONS = new HashSet<String>(
		Arrays.asList("d243936d-67ba-0a6f-6f4c-4a2a6b676d54", "855f6055-5271-1e13-ceae-336e70cf5110",
			"16dda617-1829-8555-1510-4348c162c592", "5ac082d2-3015-aba1-a749-cd928a5c6e9c",
			"77279582-ce3f-97ee-84c3-66de5af5a4c5", "01d4757e-b10e-788d-a713-176427d4f90c",
			"e8f050dd-f6c0-4cec-559f-e54d7ef19836", "5eafc9ff-cad1-7290-2bd5-bd0cb7c193ee",
			"16815372-d700-0aa8-83d6-cf9906f5a0ef", "2f6bd1b8-6ddb-3b7c-554c-646617b1dad7"));

	@Override
	public void execute(TemporaryFileHandle staging, InstitutionInfo instInfo, ConverterParams params)
	{
		removeInvalidUserPrefs(staging);
		removeInvalidConfigProps(staging);
		removeInvalidSecurityRules(staging);

		TemporaryFileHandle idefFolder = new SubTemporaryFile(staging, "itemdefinition");
		if( fileSystemService.fileExists(idefFolder) )
		{
			for( String entry : xmlHelper.getXmlFileList(idefFolder) )
			{
				final PropBagEx xml = xmlHelper.readToPropBagEx(idefFolder, entry);
				String uuid = xml.getNode("uuid");

				xml.deleteNode("embeddedXslt");
				xml.deleteNode("slow/confirmationTemplate");
				removeInvalidWizardParts(xml.getSubtree("slow/wizard"));
				if( ASSEM_COLLECTIONS.contains(uuid) )
				{
					xml.setNode("systemType", false);
				}
				xmlHelper.writeFromPropBagEx(idefFolder, entry, xml);
			}
		}

		TemporaryFileHandle psearchFolder = new SubTemporaryFile(staging, "powersearch");
		if( fileSystemService.fileExists(psearchFolder) )
		{
			for( final String entry : xmlHelper.getXmlFileList(psearchFolder) )
			{
				final PropBagEx xml = xmlHelper.readToPropBagEx(psearchFolder, entry);
				removeInvalidWizardParts(xml.getSubtree("wizard"));
				xmlHelper.writeFromPropBagEx(psearchFolder, entry, xml);
			}
		}
	}

	private void removeInvalidWizardParts(PropBagEx wiz)
	{
		removeAllNodesWithName(wiz, "background");
		removeAllNodesWithName(wiz, "styles");
		removeAllNodesWithName(wiz, "help");
	}

	private void removeAllNodesWithName(PropBagEx xml, String name)
	{
		final Iterator<PropBagEx> x = xml.iterateAllNodesWithName(name);
		while( x.hasNext() )
		{
			x.next();
			x.remove();
		}
	}

	private void removeInvalidSecurityRules(TemporaryFileHandle staging)
	{
		if( fileSystemService.fileExists(staging, "acls/entries.xml") )
		{
			PropBagEx xml = xmlHelper.readToPropBagEx(staging, "acls/entries.xml");
			for( Iterator<PropBagEx> iter = xml.iterator(); iter.hasNext(); )
			{
				final PropBagEx entry = iter.next();
				final String priv = entry.getNode("privilege");
				final String target = entry.getNode("targetObject");
				if( priv.endsWith("_ACTIVITY_WIZARD") || target.equals("C:assemblerDrmReference")
					|| target.equals("C:assesmblerFileTypes") || target.equals("C:assemblerLinks")
					|| target.equals("C:lmsexport") || target.equals("C:taxonomies") || target.equals("C:webct") )
				{
					iter.remove();
				}
			}
			xmlHelper.writeFromPropBagEx(staging, "acls/entries.xml", xml);
		}
	}

	private void removeInvalidConfigProps(TemporaryFileHandle staging)
	{
		if( fileSystemService.fileExists(staging, "properties/properties.xml") )
		{
			PropBagEx xml = xmlHelper.readToPropBagEx(staging, "properties/properties.xml");
			for( Iterator<PropBagEx> iter = xml.iterator(); iter.hasNext(); )
			{
				final String prop = iter.next().getNode("string");
				if( prop.startsWith("assembler.") || prop.startsWith("webct.") || prop.startsWith("blackboard.")
					|| prop.equals("soap.timeout") || prop.startsWith("activity-wizard.") )
				{
					iter.remove();
				}
			}
			xmlHelper.writeFromPropBagEx(staging, "properties/properties.xml", xml);
		}
	}

	private void removeInvalidUserPrefs(TemporaryFileHandle staging)
	{
		if( fileSystemService.fileExists(staging, "userprefs/preferences.xml") )
		{
			PropBagEx xml = xmlHelper.readToPropBagEx(staging, "userprefs/preferences.xml");
			for( Iterator<PropBagEx> iter = xml.iterator(); iter.hasNext(); )
			{
				if( iter.next().getNode("key/preferenceID").equals("assembler.preferences") )
				{
					iter.remove();
				}
			}
			xmlHelper.writeFromPropBagEx(staging, "userprefs/preferences.xml", xml);
		}
	}
}