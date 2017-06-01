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

package com.tle.web.workflow.notification;

import java.io.StringWriter;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.core.guice.Bind;
import com.tle.core.notification.beans.Notification;
import com.tle.core.services.item.ItemService;
import com.tle.web.i18n.BundleCache;
import com.tle.web.notification.WebNotificationExtension;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.freemarker.ExtendedFreemarkerFactory;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.result.util.ItemNameLabel;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.viewurl.ViewItemUrlFactory;

@Bind
@Singleton
@SuppressWarnings("nls")
public class StandardNotifications implements WebNotificationExtension
{
	private static final List<String> TYPES = ImmutableList.of(Notification.REASON_WENTLIVE,
		Notification.REASON_WENTLIVE2);

	@PlugKey("notereason.")
	private static String KEY_REASON_FILTER;
	@PlugKey("notificationlist.reasons.")
	private static String KEY_REASON_LIST;
	@PlugKey("emailheader.reason.")
	private static String KEY_HEADER;

	@Inject
	private BundleCache bundleCache;
	@Inject
	private ItemService itemService;
	@Inject
	private ExtendedFreemarkerFactory viewFactory;
	@Inject
	private ViewItemUrlFactory viewItemUrlFactory;

	@Override
	public boolean isIndexed(String type)
	{
		return true;
	}

	@Override
	public String emailText(ListMultimap<String, Notification> typeMap)
	{
		StringWriter writer = new StringWriter();
		for( String type : TYPES )
		{
			List<ItemNotification> itemNotifications = createItemNotifications(typeMap.get(type));
			if( !itemNotifications.isEmpty() )
			{
				final EmailNotifications model = new EmailNotifications();
				model.setHeader(new KeyLabel(KEY_HEADER + type));
				model.setNotifications(itemNotifications);
				viewFactory.render(viewFactory.createResultWithModel("notification-item.ftl", model), writer);
			}
		}
		return writer.toString();
	}

	protected List<ItemNotification> createItemNotifications(List<Notification> notifications)
	{
		List<ItemId> itemIds = Lists.newArrayList();
		for( Notification notification : notifications )
		{
			itemIds.add(new ItemId(notification.getItemid()));
		}
		List<ItemNotification> itemNotifications = Lists.newArrayList();
		Map<ItemId, Item> items = itemService.queryItemsByItemIds(itemIds);
		for( ItemId itemId : itemIds )
		{
			Item item = items.get(ItemId.fromKey(itemId));
			if( item != null )
			{
				ItemNotification task = createItemNotification(item);
				task.setItemName(new ItemNameLabel(item, bundleCache));
				task.setLink(viewItemUrlFactory.createFullItemUrl(itemId));
				itemNotifications.add(task);
			}
		}
		Collections.sort(itemNotifications, new Comparator<ItemNotification>()
		{
			@Override
			public int compare(ItemNotification o1, ItemNotification o2)
			{
				Label itemName1 = o1.getItemName();
				Label itemName2 = o2.getItemName();
				if( itemName1 == null )
				{
					return -1;
				}
				if( itemName2 == null )
				{
					return 1;
				}
				return itemName1.getText().compareToIgnoreCase(itemName2.getText());
			}
		});
		return itemNotifications;
	}

	protected ItemNotification createItemNotification(Item item)
	{
		return new ItemNotification();
	}

	public static class EmailNotifications
	{
		private Label header;
		private List<ItemNotification> notifications;

		public Label getHeader()
		{
			return header;
		}

		public void setHeader(Label header)
		{
			this.header = header;
		}

		public List<ItemNotification> getNotifications()
		{
			return notifications;
		}

		public void setNotifications(List<ItemNotification> notifications)
		{
			this.notifications = notifications;
		}

	}

	@Override
	public boolean isForceEmail(String type)
	{
		return false;
	}

	@Override
	public Label getReasonLabel(String type)
	{
		return new KeyLabel(KEY_REASON_LIST + type);
	}

	@Override
	public Label getReasonFilterLabel(String type)
	{
		return new KeyLabel(KEY_REASON_FILTER + type);
	}

}
