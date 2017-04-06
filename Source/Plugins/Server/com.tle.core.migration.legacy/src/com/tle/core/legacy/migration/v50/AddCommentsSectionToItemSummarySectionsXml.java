package com.tle.core.legacy.migration.v50;

import javax.inject.Singleton;

import com.dytech.devlib.PropBagEx;
import com.dytech.devlib.PropBagEx.PropBagIterator;
import com.tle.core.filesystem.SubTemporaryFile;
import com.tle.core.filesystem.TemporaryFileHandle;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.ConverterParams;
import com.tle.core.institution.convert.InstitutionInfo;
import com.tle.core.institution.migration.XmlMigrator;

@Bind
@Singleton
@SuppressWarnings("nls")
public class AddCommentsSectionToItemSummarySectionsXml extends XmlMigrator
{
	@Override
	public void execute(TemporaryFileHandle staging, InstitutionInfo instInfo, ConverterParams params)
	{
		TemporaryFileHandle idefFolder = new SubTemporaryFile(staging, "itemdefinition");
		for( String entry : xmlHelper.getXmlFileList(idefFolder) )
		{
			final PropBagEx xml = xmlHelper.readToPropBagEx(idefFolder, entry);

			// Add Comments Section
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
			if( config.getNode("value").equals("commentsSection") )
			{
				return;
			}
		}
		PropBagEx newSummaryConfig = xml.newSubtree("configList/com.tle.beans.entity.itemdef.SummarySectionsConfig");
		newSummaryConfig.createNode("value", "commentsSection");
		newSummaryConfig.createNode("title", "Comments");
	}

}
