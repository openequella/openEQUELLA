package com.tle.core.pss.service;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.springframework.transaction.annotation.Transactional;

import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.core.events.ItemDeletedEvent;
import com.tle.core.events.listeners.ItemDeletedListener;
import com.tle.core.guice.Bind;
import com.tle.core.pss.dao.PearsonScormServicesDao;
import com.tle.core.pss.entity.PssCallbackLog;
import com.tle.core.services.item.ItemService;

@SuppressWarnings("nls")
@Bind(PearsonScormServicesCallbackService.class)
@Singleton
public class PearsonScormServicesCallbackServiceImpl
	implements
		PearsonScormServicesCallbackService,
		ItemDeletedListener
{
	@Inject
	private PearsonScormServicesDao pssDao;
	@Inject
	private ItemService itemService;

	@Override
	@Transactional
	public void addCallbackLogEntry(PssCallbackLog logEntry)
	{
		pssDao.saveOrUpdate(logEntry);
	}

	@Override
	@Transactional
	public void deleteCallbackEntry(int trackingNumber)
	{
		PssCallbackLog logEntry = getCallbackLogEntry(trackingNumber);
		if( logEntry != null )
		{
			pssDao.delete(logEntry);
		}
	}
	
	@Override
	public void deleteCallbackEntry(Item item)
	{
		PssCallbackLog logEntry = getCallbackLogEntry(item);
		if( logEntry != null )
		{
			pssDao.delete(logEntry);
		}
	}

	@Override
	@Transactional
	public void editCallbackLogEntry(PssCallbackLog logEntry)
	{
		pssDao.saveOrUpdate(logEntry);
	}

	@Override
	@Transactional
	public PssCallbackLog getCallbackLogEntry(int trackingNumber)
	{
		return pssDao.getByTrackingNo(trackingNumber);
	}

	@Override
	@Transactional
	public PssCallbackLog getCallbackLogEntry(Item item)
	{
		return pssDao.getByItem(item);
	}

	@Override
	@Transactional
	public void itemDeletedEvent(ItemDeletedEvent event)
	{
		Item item = itemService.get(event.getItemId());
		if( item != null )
		{
			pssDao.deleteForItem(item);
		}
	}

	@Override
	public PssCallbackLog getCallbackLogEntryWithFallback(int trackingNumber, ItemId itemId)
	{
		PssCallbackLog logEntry = getCallbackLogEntry(trackingNumber);
		if( logEntry == null )
		{
			Item item = itemService.getUnsecureIfExists(itemId);
			if( item != null )
			{
				logEntry = getCallbackLogEntry(item);
			}
		}
		return logEntry;
	}
}
