package com.tle.core.institution.migration.v32;

import java.util.List;

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
public class ConvertFedSearchAttributesToBaseEntityAttributes extends XmlMigrator
{
	@Override
	public void execute(TemporaryFileHandle staging, InstitutionInfo instInfo, ConverterParams params)
	{
		final SubTemporaryFile folder = new SubTemporaryFile(staging, "federatedsearch");
		final List<String> entries = xmlHelper.getXmlFileList(folder);
		for( String entry : entries )
		{
			PropBagEx xml = xmlHelper.readToPropBagEx(folder, entry);
			for( PropBagEx pb : xml.iterateAll("attributes/" + "com.tle.beans.entity.FederatedSearch-Attribute") )
			{
				pb.setNodeName("com.tle.beans.entity.BaseEntity-Attribute");
			}
			xmlHelper.writeFromPropBagEx(folder, entry, xml);
		}
	}
}
