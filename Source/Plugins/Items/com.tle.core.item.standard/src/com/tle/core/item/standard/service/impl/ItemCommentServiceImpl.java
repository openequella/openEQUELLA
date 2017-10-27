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

package com.tle.core.item.standard.service.impl;

import java.util.Date;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.beans.item.Comment;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemIdKey;
import com.tle.beans.item.ItemKey;
import com.tle.beans.item.ItemPack;
import com.tle.common.Check;
import com.tle.common.Pair;
import com.tle.core.events.UserDeletedEvent;
import com.tle.core.events.UserEditEvent;
import com.tle.core.events.UserIdChangedEvent;
import com.tle.core.events.listeners.UserChangeListener;
import com.tle.core.guice.Bind;
import com.tle.core.guice.BindFactory;
import com.tle.core.item.dao.ItemDao;
import com.tle.core.item.operations.AbstractWorkflowOperation;
import com.tle.core.item.service.ItemService;
import com.tle.core.item.standard.ItemOperationFactory;
import com.tle.core.item.standard.dao.ItemCommentDao;
import com.tle.core.item.standard.service.ItemCommentService;
import com.tle.core.security.impl.SecureOnCall;
import com.tle.common.usermanagement.user.CurrentUser;

@Bind(ItemCommentService.class)
@Singleton
@SuppressWarnings("nls")
public class ItemCommentServiceImpl implements ItemCommentService, UserChangeListener
{
	private static final String COMMENT_VIEW_ITEM = "COMMENT_VIEW_ITEM";

	@Inject
	private ItemCommentDao dao;
	@Inject
	private ItemDao itemDao;
	@Inject
	private ItemService itemService;
	@Inject
	private ItemOperationFactory workflowFactory;
	@Inject
	private CommentOperationFactory commentOpFactory;

	@Override
	public float getAverageRatingForItem(ItemKey itemId)
	{
		return dao.getAverageRatingForItem(itemId);
	}

	@Override
	@SecureOnCall(priv = COMMENT_VIEW_ITEM)
	public Comment getComment(Item item, String commentUuid)
	{
		return dao.getByUuid(item, commentUuid);
	}

	@Override
	@Transactional
	public List<Comment> getComments(ItemKey itemId, EnumSet<CommentFilter> filter, CommentOrder order, int limit)
	{
		Item item = itemDao.getExistingItem(itemId);
		return getComments(item, filter, order, limit);
	}

	@Override
	@SecureOnCall(priv = COMMENT_VIEW_ITEM)
	public List<Comment> getComments(Item item, EnumSet<CommentFilter> filter, CommentOrder order, int limit)
	{
		filter = filter != null ? filter : EnumSet.noneOf(CommentFilter.class);
		order = order != null ? order : CommentOrder.REVERSE_CHRONOLOGICAL;

		return dao.getComments(item, filter, order, limit);
	}

	@Override
	public ItemPack<Item> addComment(ItemKey itemId, final String commentText, final int rating,
		final boolean anonymous)
	{
		return this.addComment(itemId, commentText, rating, anonymous, "");
	}

	@Override
	public ItemPack<Item> addComment(ItemKey itemId, final String commentText, final int rating,
		final boolean anonymous, final String userId)
	{
		Preconditions.checkArgument(0 <= rating && rating <= 5, "Rating must be between 0 and 5");
		return itemService.operation(itemId, commentOpFactory.comment(rating, anonymous, commentText, userId),
			commentOpFactory.average(), workflowFactory.reindexOnly(false));
	}

	@Override
	public void deleteComment(ItemKey itemId, final String commentUuid)
	{
		itemService.operation(itemId, commentOpFactory.delete(commentUuid), commentOpFactory.average(),
			workflowFactory.reindexOnly(false));
	}

	@Override
	public void userDeletedEvent(UserDeletedEvent event)
	{
		// Comments probably shouldn't disappear
	}

	@Override
	public void userEditedEvent(UserEditEvent event)
	{
		// We don't care
	}

	@Override
	@Transactional
	public void userIdChangedEvent(UserIdChangedEvent event)
	{
		ReassignOperation op = commentOpFactory.reassign(event.getFromUserId(), event.getToUserId());
		for( ItemIdKey key : dao.getItemKeysForUserComments(event.getFromUserId()) )
		{
			itemService.operation(key, op, workflowFactory.reindexOnly(false));
		}
	}

	@BindFactory
	interface CommentOperationFactory
	{
		AddCommentOperation comment(int rating, boolean anonymous, @Assisted("comment") String comment,
			@Assisted("userId") String userId);

		UpdateAverageRatingOperation average();

		DeleteCommentOperation delete(String commentUuid);

		ReassignOperation reassign(@Assisted("fromUserId") String fromUserId, @Assisted("toUserId") String toUserId);
	}
}

@SecureOnCall(priv = "COMMENT_DELETE_ITEM")
class DeleteCommentOperation extends AbstractWorkflowOperation
{
	@Inject
	private ItemCommentDao dao;
	private final String commentUuid;

	@AssistedInject
	public DeleteCommentOperation(@Assisted String commentUuid)
	{
		this.commentUuid = commentUuid;
	}

	@Override
	public boolean execute()
	{
		for( Iterator<Comment> i = getItem().getComments().iterator(); i.hasNext(); )
		{
			Comment c = i.next();
			if( c.getUuid().equals(commentUuid) )
			{
				i.remove();
				dao.delete(c);
				return true;
			}
		}
		return false;
	}
}

@SecureOnCall(priv = "COMMENT_CREATE_ITEM")
class AddCommentOperation extends AbstractWorkflowOperation
{
	private final String commentText;
	private final boolean anonymous;
	private final int rating;
	private final String userId;

	@Inject
	private ItemCommentDao dao;

	@AssistedInject
	public AddCommentOperation(@Assisted int rating, @Assisted boolean anonymous, @Assisted("comment") String comment,
		@Assisted("userId") String userId)
	{
		this.rating = rating;
		this.anonymous = anonymous;
		this.commentText = comment;
		this.userId = userId;
	}

	@Override
	public boolean execute()
	{
		Item item = getItem();
		List<Comment> ratings = item.getComments();
		String uuid = UUID.randomUUID().toString();
		Comment comment = new Comment();
		comment.setItem(item);
		comment.setRating(rating);
		comment.setAnonymous(anonymous);
		comment.setDateCreated(new Date());
		comment.setUuid(uuid);
		getItemPack().setAttribute("commentUuid", uuid);
		if( !Check.isEmpty(commentText) )
		{
			comment.setComment(commentText);
		}

		// Do not record the owner if it is a guest session or an
		// auto-logged in user since we don't know their real identity.
		// Leaving this blank also means that all guest and auto-logged
		// in user ratings will be used in the average rating
		// calculation, unlike other users where only their most recent
		// rating is used.
		if( !CurrentUser.isGuest() && !CurrentUser.wasAutoLoggedIn() )
		{
			if( !Check.isEmpty(this.userId) )
			{
				comment.setOwner(this.userId);
			}
			else
			{
				comment.setOwner(CurrentUser.getUserID());
			}
		}

		dao.save(comment);
		ratings.add(comment);

		return true;
	}
}

class UpdateAverageRatingOperation extends AbstractWorkflowOperation
{
	@Override
	public boolean isReadOnly()
	{
		return false;
	}

	@Override
	public boolean execute()
	{
		final Item item = getItem();
		final List<Comment> ratings = item.getComments();

		// Ratings of zero are not ratings, so ignore them. Also, if a user has
		// more than one rating we only want to use their most recent rating for
		// the average, unless that user was a guest session or auto-logged in
		// user, in which case we count all their ratings.
		final Map<String, Pair<Date, Integer>> userRatings = Maps.newHashMapWithExpectedSize(ratings.size());
		final List<Integer> guestRatings = Lists.newArrayList();
		for( Comment c : ratings )
		{
			final int rating = c.getRating();
			if( rating > 0 )
			{
				final String owner = c.getOwner();
				if( Check.isEmpty(owner) )
				{
					guestRatings.add(rating);
				}
				else
				{
					final Date created = c.getDateCreated();

					final Pair<Date, Integer> v = userRatings.get(owner);
					if( v == null )
					{
						userRatings.put(owner, new Pair<Date, Integer>(created, rating));
					}
					else if( created.after(v.getFirst()) )
					{
						v.setFirst(created);
						v.setSecond(rating);
					}
				}
			}
		}

		// Average needs to be -1 if there are no ratings at all - yeah, it's
		// inconsistent and if I could easily change it now, I would :(
		float average = -1;
		if( !userRatings.isEmpty() || !guestRatings.isEmpty() )
		{
			int sum = 0;
			for( Pair<Date, Integer> entry : userRatings.values() )
			{
				sum += entry.getSecond();
			}
			for( Integer entry : guestRatings )
			{
				sum += entry;
			}
			average = sum / (float) (userRatings.size() + guestRatings.size());
		}
		item.setRating(average);

		return true;
	}
}

class ReassignOperation extends AbstractWorkflowOperation
{
	@Inject
	private ItemCommentDao dao;

	private final String fromUserId;
	private final String toUserId;

	@AssistedInject
	public ReassignOperation(@Assisted("fromUserId") String fromUserId, @Assisted("toUserId") String toUserId)
	{
		this.fromUserId = fromUserId;
		this.toUserId = toUserId;
	}

	@Override
	public boolean execute()
	{
		boolean changed = false;
		for( Comment c : getItem().getComments() )
		{
			if( fromUserId.equals(c.getOwner()) )
			{
				c.setOwner(toUserId);
				dao.update(c);
				changed = true;
			}
		}
		return changed;
	}
}
