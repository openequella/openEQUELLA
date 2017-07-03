package com.tle.core.item.standard.dao;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

import com.google.common.collect.Multimap;
import com.tle.beans.item.Comment;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemIdKey;
import com.tle.beans.item.ItemKey;
import com.tle.core.hibernate.dao.GenericDao;
import com.tle.core.item.standard.service.ItemCommentService.CommentFilter;
import com.tle.core.item.standard.service.ItemCommentService.CommentOrder;

/**
 * @author Nicholas Read
 */
public interface ItemCommentDao extends GenericDao<Comment, Long>
{
	float getAverageRatingForItem(ItemKey itemId);

	Comment getByUuid(Item item, String uuid);

	List<Comment> getComments(Item item, EnumSet<CommentFilter> filter, CommentOrder order, int limit);

	Multimap<Long, Comment> getCommentsForItems(Collection<Long> itemIds);

	Collection<ItemIdKey> getItemKeysForUserComments(String userId);
}
