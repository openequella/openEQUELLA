package com.tle.web.viewitem.migration.xml.v50;

import java.util.Iterator;

import javax.inject.Singleton;

import com.dytech.devlib.PropBagEx;
import com.tle.core.filesystem.SubTemporaryFile;
import com.tle.core.filesystem.TemporaryFileHandle;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.ConverterParams;
import com.tle.core.institution.convert.InstitutionInfo;
import com.tle.core.institution.migration.XmlMigrator;

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
