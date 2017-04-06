package com.tle.core.notification.institution;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.beans.item.Item;
import com.tle.core.dao.ItemDaoExtension;
import com.tle.core.guice.Bind;
import com.tle.core.notification.dao.NotificationDao;

@Bind
@Singleton
public class NotificationItemDao implements ItemDaoExtension
{
	@Inject
	private NotificationDao dao;

	@Override
	public void delete(Item item)
	{
		dao.deleteAllForItem(item.getItemId());
	}

}
