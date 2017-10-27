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
