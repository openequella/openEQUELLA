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
public class RemoveNavigationGroupsAndLinksXml extends XmlMigrator
{
	@Override
	public void execute(TemporaryFileHandle staging, InstitutionInfo instInfo, ConverterParams params)
	{
		PropBagEx xml = xmlHelper.readToPropBagEx(staging, "acls/entries.xml");
		for( Iterator<PropBagEx> iter = xml.iterator(); iter.hasNext(); )
		{
			final String priv = iter.next().getNode("privilege");

			if( priv.endsWith("_NAVIGATION_GROUP") || priv.endsWith("_NAVIGATION_LINK") )
			{
				iter.remove();
			}
		}
		xmlHelper.writeFromPropBagEx(staging, "acls/entries.xml", xml);
	}
}