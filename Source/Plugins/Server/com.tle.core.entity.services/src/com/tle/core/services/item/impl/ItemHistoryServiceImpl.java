package com.tle.core.services.item.impl;

import java.util.List;

import javax.inject.Inject;

import org.springframework.transaction.annotation.Transactional;

import com.google.inject.Singleton;
import com.tle.beans.item.HistoryEvent;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemKey;
import com.tle.core.dao.ItemDao;
import com.tle.core.guice.Bind;
import com.tle.core.security.impl.SecureOnCall;
import com.tle.core.services.item.ItemHistoryService;

@Bind(ItemHistoryService.class)
@Singleton
public class ItemHistoryServiceImpl implements ItemHistoryService
{
	@Inject
	private ItemDao itemDao;

	@Override
	@Transactional
	public List<HistoryEvent> getHistory(ItemKey itemId)
	{
		Item item = itemDao.getExistingItem(itemId);
		return getHistory(item);
	}

	@SecureOnCall(priv = "VIEW_HISTORY_ITEM")
	protected List<HistoryEvent> getHistory(Item item)
	{
		return item.getHistory();
	}

}
