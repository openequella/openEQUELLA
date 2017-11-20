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

package com.tle.core.institution.migration.v41;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import com.dytech.devlib.PropBagEx;
import com.thoughtworks.xstream.XStream;
import com.tle.beans.entity.itemdef.DisplayTemplate;
import com.tle.beans.entity.itemdef.SummaryDisplayTemplate;
import com.tle.common.filesystem.handle.SubTemporaryFile;
import com.tle.common.filesystem.handle.TemporaryFileHandle;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.ConverterParams;
import com.tle.core.institution.convert.InstitutionInfo;
import com.tle.core.institution.convert.XmlMigrator;
import com.tle.core.xml.service.XmlService;

@Bind
@Singleton
@SuppressWarnings("nls")
public class DisplayTemplateXmlMigrator extends XmlMigrator
{
	@Inject
	private XmlService xmlService;
	@Inject
	private DisplayTemplateMigration displayTemplateMigration;

	private XStream xstream;

	@PostConstruct
	protected void setupXStream()
	{
		xstream = xmlService.createDefault(getClass().getClassLoader());
		xstream.alias("itemSummaryTemplate", DisplayTemplate.class);
	}

	@Override
	public void execute(TemporaryFileHandle staging, InstitutionInfo instInfo, ConverterParams params)
	{
		final SubTemporaryFile itemDefFolder = new SubTemporaryFile(staging, "itemdefinition");
		for( String entry : xmlHelper.getXmlFileList(itemDefFolder) )
		{
			PropBagEx itemDef = xmlHelper.readToPropBagEx(itemDefFolder, entry);

			PropBagEx summaryTemplate = itemDef.getSubtree("slow/itemSummaryTemplate");
			DisplayTemplate oldTemplate = new DisplayTemplate();
			if( summaryTemplate != null )
			{
				oldTemplate = (DisplayTemplate) xstream.fromXML(summaryTemplate.toString(), oldTemplate);
			}
			SummaryDisplayTemplate newTemplate = displayTemplateMigration.convertToNew(oldTemplate);
			String newXml = xstream.toXML(new TempBlobs(newTemplate));
			itemDef.deleteNode("slow/itemSummaryTemplate");
			itemDef.appendChildren("slow", new PropBagEx(newXml));
			xmlHelper.writeFromPropBagEx(itemDefFolder, entry, itemDef);
		}
	}

	public static class TempBlobs
	{
		public TempBlobs(SummaryDisplayTemplate newTemplate)
		{
			// xstream will use reflection to get at the internals
			this.itemSummarySections = newTemplate; // NOSONAR
		}

		SummaryDisplayTemplate itemSummarySections;
	}
}
