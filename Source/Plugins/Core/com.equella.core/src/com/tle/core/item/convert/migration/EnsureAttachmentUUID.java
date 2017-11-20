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
