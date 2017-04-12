package com.tle.core.hierarchy.migration;

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
@SuppressWarnings("nls")
public class RemoveCoursesMigrationXml extends XmlMigrator
{
	@Override
	public void execute(TemporaryFileHandle staging, InstitutionInfo instInfo, ConverterParams params)
	{
		TemporaryFileHandle hierFolder = new SubTemporaryFile(staging, "hierarchy");
		for( String entry : xmlHelper.getXmlFileList(hierFolder) )
		{
			final PropBagEx xml = xmlHelper.readToPropBagEx(hierFolder, entry);

			// This works if there is one topic per file (new format)
			removeObsoleteXml(xml);

			// This works if there are many topics per file (old format)
			if( xml.nodeExists("com.tle.beans.hierarchy.HierarchyTopic") )
			{
				for( PropBagEx subxml : xml.iterator("com.tle.beans.hierarchy.HierarchyTopic") )
				{
					removeObsoleteXml(subxml);
				}
			}

			xmlHelper.writeFromPropBagEx(hierFolder, entry, xml);
		}
	}

	private void removeObsoleteXml(PropBagEx xml)
	{
		xml.deleteNode("courses");
		xml.deleteNode("parent/courses");
	}
}
