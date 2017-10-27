/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.core.notification.standard.institution;

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
