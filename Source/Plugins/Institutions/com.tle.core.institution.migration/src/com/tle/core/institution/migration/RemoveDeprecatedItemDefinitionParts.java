package com.tle.core.institution.migration;

import java.util.List;

import javax.inject.Singleton;

import com.dytech.devlib.PropBagEx;
import com.tle.common.filesystem.handle.SubTemporaryFile;
import com.tle.common.filesystem.handle.TemporaryFileHandle;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.ConverterParams;
import com.tle.core.institution.convert.InstitutionInfo;
import com.tle.core.institution.convert.XmlMigrator;

/**
 * @author Nicholas Read
 */

@Bind
@Singleton
@SuppressWarnings("nls")
public class RemoveDeprecatedItemDefinitionParts extends XmlMigrator
{
	@Override
	public void execute(TemporaryFileHandle staging, InstitutionInfo instInfo, ConverterParams params)
	{
		final SubTemporaryFile itemsFolder = new SubTemporaryFile(staging, "itemdefinition");
		final List<String> entries = xmlHelper.getXmlFileList(itemsFolder);
		for( String entry : entries )
		{
			PropBagEx xml = xmlHelper.readToPropBagEx(itemsFolder, entry);
			xml.deleteNode("identifier");
			xml.deleteNode("digitalRights");

			for( PropBagEx page : xml.iterateAll("slow/wizard/pages/com.dytech.edge.wizard.beans.CALPage") )
			{
				page.deleteAll("bind");
			}

			xmlHelper.writeFromPropBagEx(itemsFolder, entry, xml);
		}
	}
}
