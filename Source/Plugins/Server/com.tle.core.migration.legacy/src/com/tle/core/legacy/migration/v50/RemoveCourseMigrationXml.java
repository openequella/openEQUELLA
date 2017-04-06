package com.tle.core.legacy.migration.v50;

import java.util.Iterator;

import javax.inject.Singleton;

import com.dytech.devlib.PropBagEx;
import com.tle.core.filesystem.TemporaryFileHandle;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.ConverterParams;
import com.tle.core.institution.convert.InstitutionInfo;
import com.tle.core.institution.migration.XmlMigrator;

@Bind
@Singleton
@SuppressWarnings("nls")
public class RemoveCourseMigrationXml extends XmlMigrator
{
	@Override
	public void execute(TemporaryFileHandle staging, InstitutionInfo instInfo, ConverterParams params)
	{
		if( fileSystemService.fileExists(staging, "acls/entries.xml") )
		{
			boolean changed = false;
			PropBagEx xml = xmlHelper.readToPropBagEx(staging, "acls/entries.xml");
			for( Iterator<PropBagEx> iter = xml.iterator(); iter.hasNext(); )
			{
				if( iter.next().getNode("privilege").equals("LAUNCH_ACTIVITY_MANAGER") )
				{
					iter.remove();
					changed |= true;
				}
			}

			if( changed )
			{
				xmlHelper.writeFromPropBagEx(staging, "acls/entries.xml", xml);
			}
		}
	}
}