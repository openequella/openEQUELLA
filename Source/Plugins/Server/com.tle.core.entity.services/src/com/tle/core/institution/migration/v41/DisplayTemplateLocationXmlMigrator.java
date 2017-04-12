package com.tle.core.institution.migration.v41;

import java.util.Objects;

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
public class DisplayTemplateLocationXmlMigrator extends XmlMigrator
{
	@Override
	public void execute(TemporaryFileHandle staging, InstitutionInfo instInfo, ConverterParams params)
	{
		TemporaryFileHandle idefFolder = new SubTemporaryFile(staging, "itemdefinition");

		for( String entry : xmlHelper.getXmlFileList(idefFolder) )
		{
			final PropBagEx xml = xmlHelper.readToPropBagEx(idefFolder, entry);

			// Chop off the .xml from entry
			String path = entry.substring(0, entry.length() - 4);
			folderRename(xml.getSubtree("slow/itemSummarySections"), idefFolder, path);

			xmlHelper.writeFromPropBagEx(idefFolder, entry, xml);
		}
	}

	private void folderRename(PropBagEx xml, TemporaryFileHandle idefFolder, String path)
	{
		final String st = "summarytemplate";
		final String dt = "displaytemplate";

		PropBagIterator iter = xml.iterator("configList/com.tle.beans.entity.itemdef.SummarySectionsConfig");

		for( PropBagEx config : iter )
		{
			if( config.getNode("value").equals("xsltSection") ) //$NON-NLS-2$
			{
				// Get template folder path
				String folderPath = String.format("%s/", path);

				if( !Objects.equals(folderPath, "") )
				{
					// Get template folder
					String folder = config.getNode("configuration");
					folder = folder.substring(0, folder.indexOf('/'));

					// Rename folder
					fileSystemService.rename(idefFolder, folderPath + folder, folderPath + dt);

					// Update XML
					String newFolderPath = config.getNode("configuration").replace(st, dt);
					config.setNode("configuration", newFolderPath);
				}
			}
		}
	}
}
