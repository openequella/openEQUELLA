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

package com.tle.core.institution.migration.v64;

import java.util.UUID;

import javax.inject.Singleton;

import com.dytech.devlib.PropBagEx;
import com.dytech.devlib.PropBagEx.PropBagIterator;
import com.tle.common.Check;
import com.tle.common.i18n.InternalI18NString;
import com.tle.common.i18n.KeyString;
import com.tle.core.filesystem.SubTemporaryFile;
import com.tle.core.filesystem.TemporaryFileHandle;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.ConverterParams;
import com.tle.core.institution.convert.InstitutionInfo;
import com.tle.core.institution.migration.XmlMigrator;

@Bind
@Singleton
@SuppressWarnings("nls")
public class DisplayTempateTitleXmlMigrator extends XmlMigrator
{
	@Override
	public void execute(TemporaryFileHandle staging, InstitutionInfo instInfo, ConverterParams params) throws Exception
	{
		TemporaryFileHandle idefFolder = new SubTemporaryFile(staging, "itemdefinition");
		for( String entry : xmlHelper.getXmlFileList(idefFolder) )
		{
			final PropBagEx xml = xmlHelper.readToPropBagEx(idefFolder, entry);

			// Add uuid and bundleTitle to all Sections
			addSummarySection(xml.getSubtree("slow/itemSummarySections"));

			xmlHelper.writeFromPropBagEx(idefFolder, entry, xml);
		}
	}

	private void addSummarySection(PropBagEx xml)
	{
		PropBagIterator iter = xml.iterator("configList/com.tle.beans.entity.itemdef.SummarySectionsConfig");
		while( iter.hasNext() )
		{
			PropBagEx config = iter.next();
			if( Check.isEmpty(config.getNode("uuid")) )
			{
				config.createNode("uuid", UUID.randomUUID().toString());
			}

			if( config.getSubtree("bundleTitle") != null )
			{
				continue;
			}

			String configValue = config.getNode("value");
			String title = config.getNode("bundleTitle");
			switch( configValue )
			{
				case "attachmentsSection":
					InternalI18NString attachmentTitle = new KeyString(
						"com.tle.web.viewitem.summary.summary.content.attachments.title");
					DisplayTempateTitleMigration.addNewBundleTitle(attachmentTitle.toString(), config);
					break;
				case "commentsSection":
					InternalI18NString commentTitle = new KeyString("com.tle.web.viewitem.summary.comments.addnew");
					DisplayTempateTitleMigration.addNewBundleTitle(commentTitle.toString(), config);
					break;
				case "citationSummarySection":
					InternalI18NString citationTitle = new KeyString("com.tle.cal.web.citation.summary.title");
					DisplayTempateTitleMigration.addNewBundleTitle(citationTitle.toString(), config);
					break;
				default:
					if( !Check.isEmpty(title) )
					{
						DisplayTempateTitleMigration.addNewBundleTitle(title, config);
					}
					else
					{
						DisplayTempateTitleMigration.addOtherSectionNames(configValue, config);
					}
					break;
			}
		}
	}
}
