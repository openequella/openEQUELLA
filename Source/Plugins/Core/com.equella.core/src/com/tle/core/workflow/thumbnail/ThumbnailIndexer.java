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

package com.tle.core.workflow.thumbnail;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dytech.edge.queries.FreeTextQuery;
import com.tle.annotation.NonNullByDefault;
import com.tle.beans.item.Item;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.beans.item.attachments.UnmodifiableAttachments;
import com.tle.core.freetext.indexer.AbstractIndexingExtension;
import com.tle.core.guice.Bind;
import com.tle.core.mimetypes.MimeTypeService;
import com.tle.freetext.IndexedItem;

/**
 * @author Aaron
 *
 */
@NonNullByDefault
@Bind
@Singleton
public class ThumbnailIndexer extends AbstractIndexingExtension
{
	private static final Logger LOGGER = LoggerFactory.getLogger(ThumbnailIndexer.class);

	@Inject
	private MimeTypeService mimeTypeService;

	@Override
	public void indexFast(IndexedItem indexedItem)
	{
		final Item item = indexedItem.getItem();
		final UnmodifiableAttachments attachments = new UnmodifiableAttachments(item);

		boolean indexedRealThumb = false;
		final String thumb = item.getThumb();
		if( thumb != null )
		{
			if( thumb.startsWith("custom:") )
			{
				String thumbUuid = thumb.split(":")[1];
				Attachment attachment = ((Attachment) attachments.getAttachmentByUuid(thumbUuid));
				if( attachment != null )
				{
					indexedRealThumb = true;
					indexedItem.getItemdoc().add(
						keyword(FreeTextQuery.FIELD_REAL_THUMB, Boolean.toString(attachmentHasRealThumb(attachment))));
				}
			}
			else if( thumb.equals("default") )
			{
				for( IAttachment attachment : attachments )
				{
					// at least one image attachment w/thumbnails
					if( attachmentHasRealThumb((Attachment) attachment) )
					{
						indexedRealThumb = true;
						indexedItem.getItemdoc().add(keyword(FreeTextQuery.FIELD_REAL_THUMB, "true"));
						break;
					}
				}
			}
			else if( thumb.equals("initial") )
			{
				LOGGER.trace("Thumb value initial, not indexing 'real thumb'");
				//We didn't, but let's pretend we did since this is a real world case
				indexedRealThumb = true;
			}
			else
			{
				LOGGER.warn("Unknown thumb value '" + thumb + "' on item " + item.getId());
			}
		}
		else
		{
			LOGGER.error("No thumbnail for item with DB id: " + item.getId());
		}
		if( !indexedRealThumb )
		{
			LOGGER.debug("Did not index realThumb for item with DB id: " + item.getId());
		}
	}

	private boolean attachmentHasRealThumb(Attachment attachment)
	{
		String thumb = attachment.getThumbnail();
		if( !"suppress".equals(thumb) )
		{
			String mime = mimeTypeService.getMimeEntryForAttachment(attachment);
			return mime != null && mime.startsWith("image/");
		}
		return false;
	}

	@Override
	public void indexSlow(IndexedItem indexedItem)
	{
		// Nah
	}

	@Override
	public void loadForIndexing(List<IndexedItem> items)
	{
		// Nah
	}

}
