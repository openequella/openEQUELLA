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

package com.tle.web.scorm.migration;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.dytech.devlib.PropBagEx;
import com.tle.common.Check;
import com.tle.common.filesystem.handle.SubTemporaryFile;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.AbstractItemXmlMigrator;
import com.tle.core.institution.convert.ConverterParams;
import com.tle.core.services.FileSystemService;

@Bind
@Singleton
public class ConvertIMSToScormXmlMigration extends AbstractItemXmlMigrator
{

	@Inject
	private FileSystemService fileSystemService;

	@SuppressWarnings("nls")
	@Override
	public boolean migrate(ConverterParams params, PropBagEx xml, SubTemporaryFile file, String filename)
		throws Exception
	{
		boolean modified = false;

		String scormVersion = xml.getNode("attachments/com.tle.beans.item.attachments.ImsAttachment/value2");
		if( !Check.isEmpty(scormVersion) )
		{
			modified = true;
			PropBagEx attachments = xml.aquireSubtree("attachments");
			PropBagEx ims = attachments.aquireSubtree("com.tle.beans.item.attachments.ImsAttachment");
			String size = ims.getNode("value1");

			ims.setNodeName("com.tle.beans.item.attachments.CustomAttachment");
			ims.setNode("value1", "scorm");
			ims.deleteNode("value2");
			PropBagEx data = ims.newSubtree("data");
			PropBagEx entry = data.newSubtree("entry");
			entry.createNode("string", "fileSize");
			if( size.isEmpty() )
			{
				size = "0";
			}
			entry.createNode("long", size);

			entry = data.newSubtree("entry");
			entry.createNode("string", "SCORM_VERSION");
			entry.createNode("string", scormVersion);

			for( PropBagEx imsRes : attachments.iterateAll("com.tle.beans.item.attachments.IMSResourceAttachment") )
			{
				imsRes.setNodeName("com.tle.beans.item.attachments.CustomAttachment");
				imsRes.setNode("value1", "scormres");
			}

			PropBagEx treeNodes = xml.aquireSubtree("treeNodes");
			String string = treeNodes.toString();
			string = string.replaceAll("com.tle.beans.item.attachments.IMSResourceAttachment",
				"com.tle.beans.item.attachments.CustomAttachment");
			PropBagEx newTreeNodes = new PropBagEx(string);
			xml.deleteSubtree(treeNodes);
			xml.appendChildren("treeNodes", newTreeNodes);
			fileSystemService.rename(getDataFolder(file, filename), "_IMS", "_SCORM");
		}

		return modified;
	}
}
