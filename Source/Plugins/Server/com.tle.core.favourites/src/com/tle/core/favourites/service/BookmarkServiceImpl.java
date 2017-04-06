package com.tle.core.favourites.service;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.transaction.annotation.Transactional;

import com.tle.beans.item.Bookmark;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemKey;
import com.tle.common.Check;
import com.tle.core.events.UserDeletedEvent;
import com.tle.core.events.UserEditEvent;
import com.tle.core.events.UserIdChangedEvent;
import com.tle.core.events.listeners.UserChangeListener;
import com.tle.core.favourites.dao.BookmarkDao;
import com.tle.core.guice.Bind;
import com.tle.core.services.item.ItemService;
import com.tle.core.user.CurrentInstitution;
import com.tle.core.user.CurrentUser;
import com.tle.core.workflow.operations.WorkflowFactory;

@SuppressWarnings("nls")
@Bind(BookmarkService.class)
@Singleton
public class BookmarkServiceImpl implements BookmarkService, UserChangeListener
{
	@Inject
	private BookmarkDao dao;
	@Inject
	private ItemService itemService;
	@Inject
	private WorkflowFactory workflowFactory;

	@Override
	public Bookmark getByItem(ItemKey itemId)
	{
		return dao.getByItemAndUserId(CurrentUser.getUserID(), itemId);
	}

	@Override
	@Transactional
	public void add(Item item, String tagString, boolean latest)
	{
		Set<String> keywords = new HashSet<String>();

		if( !Check.isEmpty(tagString) )
		{
			String tags[] = tagString.split("\\s|,|;");
			for( int i = 0; i < tags.length; i++ )
			{
				if( !Check.isEmpty(tags[i]) )
				{
					keywords.add(tags[i].toLowerCase());
				}
			}
		}

		Bookmark bookmark = new Bookmark();
		bookmark.setItem(item);
		bookmark.setKeywords(keywords);
		bookmark.setOwner(CurrentUser.getUserID());
		bookmark.setInstitution(CurrentInstitution.get());
		bookmark.setDateModified(new Date());
		bookmark.setAlwaysLatest(latest);
		dao.save(bookmark);

		itemService.operation(bookmark.getItem().getItemId(), workflowFactory.reindexOnly(true));
	}

	@Override
	@Transactional
	public void delete(long id)
	{
		Bookmark bookmark = dao.findById(id);
		dao.delete(bookmark);
		itemService.operation(bookmark.getItem().getItemId(), workflowFactory.reindexOnly(true));
	}

	@Override
	@Transactional
	public List<Item> filterNonBookmarkedItems(Collection<Item> items)
	{
		return dao.filterNonBookmarkedItems(items);
	}

	@Override
	@Transactional
	public Map<Item, Bookmark> getBookmarksForItems(Collection<Item> items)
	{
		return dao.getBookmarksForItems(items, CurrentUser.getUserID());
	}

	@Override
	public List<Bookmark> getBookmarksForOwner(String ownerUuid, int maxResults)
	{
		return dao.findAllByCriteria(Order.desc("dateModified"), maxResults, Restrictions.eq("owner", ownerUuid),
			Restrictions.eq("institution", CurrentInstitution.get()));
	}

	@Override
	@Transactional
	public void userDeletedEvent(UserDeletedEvent event)
	{
		dao.deleteAllForUser(event.getUserID());
	}

	@Override
	@Transactional
	public void userIdChangedEvent(UserIdChangedEvent event)
	{
		dao.changeOwnership(event.getFromUserId(), event.getToUserId());
	}

	@Override
	public void userEditedEvent(UserEditEvent event)
	{
		// Nothing to do here
	}
}
