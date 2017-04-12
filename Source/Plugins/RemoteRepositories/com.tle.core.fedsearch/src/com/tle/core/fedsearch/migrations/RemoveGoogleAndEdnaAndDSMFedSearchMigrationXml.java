package com.tle.core.fedsearch.migrations;

import java.util.List;

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
public class RemoveGoogleAndEdnaAndDSMFedSearchMigrationXml extends XmlMigrator
{
	// and LORN!
	@Override
	@SuppressWarnings("nls")
	public void execute(TemporaryFileHandle staging, InstitutionInfo instInfo, ConverterParams params)
	{
		final SubTemporaryFile fedSearchFolder = new SubTemporaryFile(staging, "federatedsearch");
		final List<String> fedSearches = xmlHelper.getXmlFileList(fedSearchFolder);
		for( String fedSearch : fedSearches )
		{
			PropBagEx xml = xmlHelper.readToPropBagEx(fedSearchFolder, fedSearch);
			String node = xml.getNode("type");
			if( node.equals("GoogleSearchEngine") || node.equals("EdnaOnlineSearchEngine")
				|| node.equals("DSMSearchEngine") || node.equals("LornSearchEngine") )
			{
				fileSystemService.removeFile(fedSearchFolder, fedSearch);
			}
		}
	}
}
