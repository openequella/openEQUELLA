package com.tle.web.workflow.notification;

import java.io.StringWriter;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.inject.Singleton;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.ItemTaskId;
import com.tle.core.guice.Bind;
import com.tle.core.i18n.BundleCache;
import com.tle.core.item.service.ItemService;
import com.tle.core.notification.beans.Notification;
import com.tle.core.notification.standard.service.NotificationPreferencesService;
import com.tle.web.sections.SectionsController;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.freemarker.ExtendedFreemarkerFactory;
import com.tle.web.sections.result.util.ItemNameLabel;
import com.tle.web.sections.result.util.PluralKeyLabel;
import com.tle.web.workflow.tasks.RootTaskListSection;

@Bind
@Singleton
public class ScriptTaskNotification extends StandardNotifications
{
	private static final List<String> TYPES = ImmutableList.of(Notification.REASON_SCRIPT_ERROR,
		Notification.REASON_SCRIPT_EXECUTED);

	@PlugKey("emailheader.reason.error")
	private static String KEY_HEADER_ERROR;
	@PlugKey("emailheader.reason.executed")
	private static String KEY_HEADER_EXECUTED;

	@Inject
	private ExtendedFreemarkerFactory viewFactory;
	@Inject
	private ItemService itemService;
	@Inject
	private BundleCache bundleCache;
	@Inject
	private SectionsController controller;
	@Inject
	private NotificationPreferencesService workflowPrefs;

	@Override
	public String emailText(ListMultimap<String, Notification> typeMap)
	{
		StringWriter writer = new StringWriter();
		for( String type : TYPES )
		{
			List<ItemNotification> itemNotifications = createItemNotifications(typeMap.get(type), type);
			if( !itemNotifications.isEmpty() )
			{
				PluralKeyLabel header = null;
				if( type == Notification.REASON_SCRIPT_EXECUTED )
				{
					header = new PluralKeyLabel(KEY_HEADER_EXECUTED, itemNotifications.size());
				}
				else
				{
					header = new PluralKeyLabel(KEY_HEADER_ERROR, itemNotifications.size());
				}

				final EmailNotifications model = new EmailNotifications();
				model.setHeader(header);
				model.setNotifications(itemNotifications);
				viewFactory.render(viewFactory.createResultWithModel("notification-script.ftl", model), writer);
			}
		}
		return writer.toString();
	}

	protected List<ItemNotification> createItemNotifications(List<Notification> notifications, String type)
	{
		Set<String> optedOutCollections = workflowPrefs.getOptedOutCollections();

		List<ItemTaskId> itemTaskIds = Lists.newArrayList();
		for( Notification notification : notifications )
		{
			itemTaskIds.add(new ItemTaskId(notification.getItemid()));
		}

		List<ItemNotification> itemNotifications = Lists.newArrayList();
		Map<ItemId, Item> items = itemService.queryItemsByItemIds(itemTaskIds);
		for( ItemTaskId itemTaskId : itemTaskIds )
		{
			Item item = items.get(ItemId.fromKey(itemTaskId));

			if( item != null )
			{
				ItemDefinition itemDef = item.getItemDefinition();
				if( optedOutCollections.contains(itemDef.getUuid())
					&& type.equals(Notification.REASON_SCRIPT_EXECUTED) )
				{
					continue;
				}
				ItemNotification task = createItemNotification(item);
				task.setItemName(new ItemNameLabel(item, bundleCache));
				task.setLink(RootTaskListSection.createModerateBookmark(controller, itemTaskId));
				itemNotifications.add(task);
			}
		}
		return itemNotifications;
	}

	@Override
	public int countNotification(ListMultimap<String, Notification> typeMap)
	{
		Set<String> optedoutCollections = workflowPrefs.getOptedOutCollections();
		int count = 0;
		for( String type : TYPES )
		{
			List<ItemTaskId> itemTaskIds = Lists.newArrayList();
			List<Notification> notifications = typeMap.get(type);
			for( Notification notification : notifications )
			{
				itemTaskIds.add(new ItemTaskId(notification.getItemid()));
			}
			Map<ItemId, Item> items = itemService.queryItemsByItemIds(itemTaskIds);
			for( ItemTaskId itemTaskId : itemTaskIds )
			{
				Item item = items.get(ItemId.fromKey(itemTaskId));
				ItemDefinition itemDef = item.getItemDefinition();
				if( !optedoutCollections.contains(itemDef.getUuid()) || type.equals(Notification.REASON_SCRIPT_ERROR) )
				{
					count++;
				}
			}
		}
		return count;
	}
}
