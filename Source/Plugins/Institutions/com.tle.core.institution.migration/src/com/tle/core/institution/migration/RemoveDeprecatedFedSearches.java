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

@Bind
@Singleton
public class RemoveDeprecatedFedSearches extends XmlMigrator
{
	@Override
	public void execute(TemporaryFileHandle staging, InstitutionInfo instInfo, ConverterParams params) throws Exception
	{
		final SubTemporaryFile fedSearchFolder = new SubTemporaryFile(staging, "federatedsearch");
		final List<String> fedSearches = xmlHelper.getXmlFileList(fedSearchFolder);
		for( String fedSearch : fedSearches )
		{
			PropBagEx xml = xmlHelper.readToPropBagEx(fedSearchFolder, fedSearch);
			if( xml.getNode("type").equals("DiscoverSearchEngine") )
			{
				fileSystemService.removeFile(fedSearchFolder, fedSearch);
			}
		}
	}
}
