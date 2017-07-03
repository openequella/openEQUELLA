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

package com.tle.core.wizard.controls.universal.migration.v60;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;
import com.google.inject.Singleton;
import com.tle.beans.item.Item;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.AttachmentType;
import com.tle.beans.item.attachments.ZipAttachment;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.PostReadMigrator;
import com.tle.core.item.convert.ItemConverter.ItemConverterInfo;

@Bind
@Singleton
public class MigrateZipFilesImport implements PostReadMigrator<ItemConverterInfo>
{

	@SuppressWarnings("nls")
	@Override
	public void migrate(ItemConverterInfo obj) throws IOException
	{
		Item item = obj.getItem();
		List<Attachment> attachments = item.getAttachments();
		Map<String, String> zipMap = Maps.newHashMap();
		for( Attachment attachment : attachments )
		{
			String attachUrl = attachment.getUrl();
			if( attachment.getAttachmentType() == AttachmentType.ZIP && attachUrl.startsWith("_zips/") )
			{
				String zipFile = attachUrl.substring(6);
				zipMap.put(zipFile, attachment.getUuid());
			}
		}
		for( Attachment attachment : attachments )
		{
			String attachUrl = attachment.getUrl();
			int afterSlash = attachUrl.indexOf('/');
			if( attachment.getAttachmentType() == AttachmentType.FILE && afterSlash != -1 )
			{
				@SuppressWarnings("null")
				String zipFile = attachUrl.substring(0, afterSlash);
				String zipUuid = zipMap.get(zipFile);
				if( zipUuid != null )
				{
					attachment.setData(ZipAttachment.KEY_ZIP_ATTACHMENT_UUID, zipUuid);
				}
			}
		}
	}

}
