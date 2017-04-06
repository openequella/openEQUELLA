package com.tle.core.legacy.migration.v50;

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
public class RemoveObsoleteCollectionSettingsXml extends XmlMigrator
{
	@Override
	public void execute(TemporaryFileHandle staging, InstitutionInfo instInfo, ConverterParams params)
	{
		TemporaryFileHandle idefFolder = new SubTemporaryFile(staging, "itemdefinition");

		for( String entry : xmlHelper.getXmlFileList(idefFolder) )
		{
			final PropBagEx xml = xmlHelper.readToPropBagEx(idefFolder, entry);
			removeObsoleteXml(xml);
			xmlHelper.writeFromPropBagEx(idefFolder, entry, xml);
		}
	}

	private void removeObsoleteXml(PropBagEx xml)
	{
		xml.deleteNode("iconPath");
		xml.deleteNode("iconUploaded");
		xml.deleteNode("searchResultId");
		xml.deleteNode("thumbnailMax");
		xml.deleteNode("thumbnailHeight");
		xml.deleteNode("type");
	}
}
