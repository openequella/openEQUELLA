package com.tle.ims.migration;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.beans.filesystem.FileHandle;
import com.tle.beans.item.Item;
import com.tle.beans.item.attachments.AttachmentType;
import com.tle.beans.item.attachments.Attachments;
import com.tle.beans.item.attachments.ImsAttachment;
import com.tle.beans.item.attachments.UnmodifiableAttachments;
import com.tle.common.Check;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.ItemConverter;
import com.tle.core.institution.migration.PostReadMigrator;
import com.tle.core.util.ims.IMSNavigationHelper;
import com.tle.core.util.ims.beans.IMSManifest;
import com.tle.ims.service.IMSService;

@Bind
@Singleton
public class IMSNavigationCreation implements PostReadMigrator<ItemConverter.ItemConverterInfo>
{
	@Inject
	private IMSService imsService;
	@Inject
	private IMSNavigationHelper navHelper;

	@Override
	public void migrate(ItemConverter.ItemConverterInfo info) throws IOException
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
