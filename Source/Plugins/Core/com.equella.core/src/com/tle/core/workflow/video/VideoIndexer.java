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

package com.tle.core.workflow.video;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.dytech.edge.queries.FreeTextQuery;
import com.tle.annotation.NonNullByDefault;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.core.freetext.indexer.AbstractIndexingExtension;
import com.tle.core.guice.Bind;
import com.tle.freetext.IndexedItem;

/**
 * @author Aaron
 *
 */
@NonNullByDefault
@Bind
@Singleton
public class VideoIndexer extends AbstractIndexingExtension
{
	@Inject
	private VideoService videoService;

	@Override
	public void indexFast(IndexedItem indexedItem)
	{
		for( IAttachment attachment : indexedItem.getItem().getAttachments() )
		{
			final String thumb = attachment.getThumbnail();
			if( !"suppress".equals(thumb) )
			{
				if( videoService.isVideo((Attachment) attachment) )
				{
					indexedItem.getItemdoc().add(keyword(FreeTextQuery.FIELD_VIDEO_THUMB, "true"));
					break;
				}
			}
		}
	}

	@Override
	public void indexSlow(IndexedItem indexedItem)
	{
		//Nah
	}

	@Override
	public void loadForIndexing(List<IndexedItem> items)
	{
		//Nah
	}
}
