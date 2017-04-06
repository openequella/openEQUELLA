package com.tle.core.payment.storefront.migration.forimport;

import java.util.List;

import javax.inject.Singleton;

import com.dytech.devlib.PropBagEx;
import com.tle.core.filesystem.SubTemporaryFile;
import com.tle.core.filesystem.TemporaryFileHandle;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.ConverterParams;
import com.tle.core.institution.convert.InstitutionInfo;
import com.tle.core.institution.migration.XmlMigrator;

/**
 * @author Aaron
 */
@Bind
@Singleton
public class RemoveStoreIconUrlFieldXmlMigration extends XmlMigrator
{
	@SuppressWarnings("nls")
	@Override
	public void execute(TemporaryFileHandle staging, InstitutionInfo instInfo, ConverterParams params)
	{
		SubTemporaryFile folder = new SubTemporaryFile(staging, "store");
		List<String> xmlFileList = xmlHelper.getXmlFileList(folder);
		for( String storeXmlFile : xmlFileList )
		{
			PropBagEx storeXml = xmlHelper.readToPropBagEx(folder, storeXmlFile);
			storeXml.deleteNode("icon");
			xmlHelper.writeFromPropBagEx(folder, storeXmlFile, storeXml);
		}
	}
}
