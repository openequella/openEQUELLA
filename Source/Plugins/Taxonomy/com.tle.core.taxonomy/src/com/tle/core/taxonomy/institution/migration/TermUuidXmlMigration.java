package com.tle.core.taxonomy.institution.migration;

import java.util.UUID;

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
public class TermUuidXmlMigration extends XmlMigrator
{

	@Override
	public void execute(TemporaryFileHandle staging, InstitutionInfo instInfo, ConverterParams params) throws Exception
	{
		SubTemporaryFile taxonomyFolder = new SubTemporaryFile(staging, "taxonomy2");
	
		// Path: taxonomy2/*bucket*/*taxonomy-uuid*/terms/*#(directory)*/#.xml
		for( String entry : fileSystemService.grep(taxonomyFolder, "", "*/*/terms/*/*.xml") )
		{
			PropBagEx xml = xmlHelper.readToPropBagEx(taxonomyFolder, entry);
			if( !xml.nodeExists("uuid") )
			{
				xml.createNode("uuid", UUID.randomUUID().toString());
				xmlHelper.writeFromPropBagEx(taxonomyFolder, entry, xml);
			}

		}
	}

}
