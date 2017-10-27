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

package com.tle.core.notification.standard.indexer;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.Document;

import com.tle.beans.item.ItemId;
import com.tle.common.util.Dates;
import com.tle.common.util.UtcDate;
import com.tle.core.freetext.indexer.AbstractIndexingExtension;
import com.tle.core.guice.Bind;
import com.tle.core.notification.NotificationExtension;
import com.tle.core.notification.NotificationService;
import com.tle.core.notification.beans.Notification;
import com.tle.core.notification.dao.NotificationDao;
import com.tle.freetext.IndexedItem;

@Bind
@Singleton
public class NotificationIndexer extends AbstractIndexingExtension
{
	private static final Log LOGGER = LogFactory.getLog(Notification.class);
	@Inject
	private NotificationDao dao;
	@Inject
	private NotificationService notificationService;

	private static String DISCOVER_ITEM = "DISCOVER_ITEM"; //$NON-NLS-1$

	@SuppressWarnings("nls")
	@Override
	public void indexFast(IndexedItem indexedItem)
	{
		List<Notification> notifications = indexedItem.getAttribute(NotificationIndexer.class);
		List<Document> docs = indexedItem.getDocumentsForIndex(NotificationIndex.INDEXID);
		for( Notification notification : notifications )
		{
			String reason = notification.getReason();
			NotificationExtension extension = notificationService.getExtensionForType(reason);
			if( extension == null )
			{
				LOGGER.error("Unknown notification type '" + reason + "'");
			}
			else if( extension.isIndexed(reason) )
			{
				Document doc = new Document();
				addAllFields(doc, indexedItem.getBasicFields());
				doc.add(keyword(NotificationIndex.FIELD_ID, Long.toString(notification.getId())));
				doc.add(indexed(NotificationIndex.FIELD_USER, notification.getUserTo()));
				doc.add(indexed(NotificationIndex.FIELD_REASON, reason));
				doc.add(indexed(NotificationIndex.FIELD_DATE, new UtcDate(notification.getDate()).format(Dates.ISO)));
				addAllFields(doc, indexedItem.getACLEntries(DISCOVER_ITEM));
				docs.add(doc);
			}
		}
	}

	@Override
	public void indexSlow(IndexedItem indexedItem)
	{
		// no slow
	}

	@Override
	public void loadForIndexing(List<IndexedItem> items)
	{
		for( IndexedItem indexedItem : items )
		{
			List<Notification> notifications = dao.getNotificationsForItem(ItemId.fromKey(indexedItem.getItemIdKey()),
				indexedItem.getInstitution());
			indexedItem.setAttribute(NotificationIndexer.class, notifications);
		}
	}

}
