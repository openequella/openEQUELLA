package com.tle.web.notification;

import com.tle.core.guice.Bind;
import com.tle.web.itemlist.item.AbstractItemListEntry;
import com.tle.web.sections.events.RenderContext;

@Bind
public class NotificationItemListEntry extends AbstractItemListEntry
{
	private long notificationId;

	public void setNotificationId(long notificationId)
	{
		this.notificationId = notificationId;
	}

	public long getNotificationId()
	{
		return notificationId;
	}

	@Override
	protected void setupMetadata(RenderContext context)
	{
		// do nothing
	}
}
