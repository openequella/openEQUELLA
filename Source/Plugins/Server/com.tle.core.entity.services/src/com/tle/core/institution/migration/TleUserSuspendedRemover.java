package com.tle.core.institution.migration;

import javax.inject.Singleton;

import com.dytech.devlib.PropBagEx;
import com.tle.core.filesystem.TemporaryFileHandle;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.ConverterParams;
import com.tle.core.institution.convert.InstitutionInfo;

@Bind
@Singleton
@SuppressWarnings("nls")
public class TleUserSuspendedRemover extends XmlMigrator
{
	@Override
	public void execute(TemporaryFileHandle staging, InstitutionInfo instInfo, ConverterParams params)
	{
		final String xmlFile = "users/users.xml";
		if( fileExists(staging, xmlFile) )
		{
			PropBagEx xml = xmlHelper.readToPropBagEx(staging, xmlFile);
			for( PropBagEx subXml : xml.iterator() )
			{
				subXml.deleteNode("suspended");
			}
			xmlHelper.writeFromPropBagEx(staging, xmlFile, xml);
		}
	}
}
