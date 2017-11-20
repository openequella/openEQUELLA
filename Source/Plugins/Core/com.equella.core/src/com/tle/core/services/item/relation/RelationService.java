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

package com.tle.core.services.item.relation;

import java.util.Collection;
import java.util.List;

import com.tle.beans.item.Item;
import com.tle.beans.item.ItemKey;
import com.tle.beans.item.Relation;

public interface RelationService
{
	Collection<Relation> getAllByFromItem(Item from);

	Collection<Relation> getAllByToItem(Item to);

	Collection<Relation> getAllByType(String type);

	Collection<Relation> getAllByToItemAndType(Item to, String type);

	Collection<Relation> getAllByFromItemAndType(Item from, String type);

	/**
	 * NOTE: Does not fire relation listeners
	 * 
	 * @param relation
	 */
	void delete(Relation relation);

	/**
	 * NOTE: Does not fire relation listeners
	 * 
	 * @param relation
	 */
	long saveRelation(Relation relation);

	void updateRelation(Relation relation);

	Relation getById(long id);

	/**
	 * Must have VIEW_ITEM.
	 * 
	 * @param itemId
	 * @param id
	 * @return
	 */
	Relation getForView(ItemKey itemId, long id);

	/**
	 * Must have VIEW_ITEM.
	 * 
	 * @param itemId
	 * @return
	 */
	List<Relation> getRelationsForItem(ItemKey itemId);

	/**
	 * Delete a relation. Checks EDIT_ITEM on the supplied item. This will call
	 * any registered relation listeners.
	 * 
	 * @param itemId
	 * @param relationId
	 */
	void delete(ItemKey itemId, long relationId);

	/**
	 * Create a new relation. Checks EDIT_ITEM on the supplied item. This will
	 * call any registered relation listeners.
	 * 
	 * @param fromItem
	 * @param toItem
	 * @param editorIsFrom
	 * @param type
	 * @param fromResource
	 * @param toResource
	 * @return
	 */
	long createRelation(ItemKey fromItem, ItemKey toItem, boolean editorIsFrom, String type, String fromResource,
		String toResource);

}
