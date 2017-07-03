package com.tle.core.item.convert.migration;

import java.io.IOException;
import java.util.UUID;

import javax.inject.Singleton;

import com.tle.beans.item.Item;
import com.tle.beans.item.attachments.Attachment;
import com.tle.common.Check;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.PostReadMigrator;
import com.tle.core.item.convert.ItemConverter.ItemConverterInfo;

@Bind
@Singleton
public class EnsureAttachmentUUID implements PostReadMigrator<ItemConverterInfo>
{
	@Override
	public void migrate(ItemConverterInfo obj) throws IOException
	{
		Item item = obj.getItem();
		for( Attachment attachment : item.getAttachmentsUnmodifiable() )
		{
			if( Check.isEmpty(attachment.getUuid()) )
			{
				attachment.setUuid(UUID.randomUUID().toString());
			}
		}
	}
}
