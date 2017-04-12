package com.tle.core.notification.institution;

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
public class MigrateNotifications2XML extends XmlMigrator
{
	@SuppressWarnings("nls")
	@Override
	public void execute(TemporaryFileHandle staging, InstitutionInfo info, ConverterParams params)
	{
		TemporaryFileHandle notificationFolder = new SubTemporaryFile(staging, NotificationConverter.CONVERTER_FOLDER);
		for( String entry : xmlHelper.getXmlFileList(notificationFolder) )
		{
			final PropBagEx xml = xmlHelper.readToPropBagEx(notificationFolder, entry);
			xml.deleteNode("userFrom");
			xml.setNode("processed", true);
			xml.setNode("itemidOnly", xml.getNode("itemid"));
			if( xml.getNode("reason").equals("review") )
			{
				fileSystemService.removeFile(notificationFolder, entry);
			}
			else
			{
				xmlHelper.writeFromPropBagEx(notificationFolder, entry, xml);
			}
		}
		TemporaryFileHandle itemdefFolder = new SubTemporaryFile(staging, "itemdefinition");
		for( String entry : xmlHelper.getXmlFileList(itemdefFolder) )
		{
			final PropBagEx xml = xmlHelper.readToPropBagEx(itemdefFolder, entry);
			xml.deleteNode("slow/escalations");
			xmlHelper.writeFromPropBagEx(itemdefFolder, entry, xml);
		}
	}

}
