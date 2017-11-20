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

package com.tle.ims.migration;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.beans.item.Item;
import com.tle.beans.item.attachments.AttachmentType;
import com.tle.beans.item.attachments.Attachments;
import com.tle.beans.item.attachments.ImsAttachment;
import com.tle.beans.item.attachments.UnmodifiableAttachments;
import com.tle.common.Check;
import com.tle.common.filesystem.handle.FileHandle;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.PostReadMigrator;
import com.tle.core.item.convert.ItemConverter.ItemConverterInfo;
import com.tle.core.util.ims.IMSNavigationHelper;
import com.tle.core.util.ims.beans.IMSManifest;
import com.tle.ims.service.IMSService;

@Bind
@Singleton
public class IMSNavigationCreation implements PostReadMigrator<ItemConverterInfo>
{
	@Inject
	private IMSService imsService;
	@Inject
	private IMSNavigationHelper navHelper;

	@Override
	public void migrate(ItemConverterInfo info) throws IOException
	{
		Item item = info.getItem();
		FileHandle attachFiles = info.getFileHandle();
		Attachments attachments = new UnmodifiableAttachments(item);
		List<ImsAttachment> imsAttachments = attachments.getList(AttachmentType.IMS);
		if( imsAttachments.isEmpty() )
		{
			return;
		}
		ImsAttachment imsAttachment = imsAttachments.get(0);
		String szPackage = imsAttachment.getUrl();

		// treeBuilder.createTree(fileSystemService, item, attachFiles,
		// szPackage);
		IMSManifest manifest = imsService.getImsManifest(attachFiles, szPackage, true);
		String scormVersion = imsService.getScormVersion(attachFiles, szPackage);

		boolean expand = imsAttachment.isExpand();

		if( manifest != null )
		{
			navHelper.createTree(manifest, item, attachFiles, szPackage, !Check.isEmpty(scormVersion), expand);
		}
	}
}
