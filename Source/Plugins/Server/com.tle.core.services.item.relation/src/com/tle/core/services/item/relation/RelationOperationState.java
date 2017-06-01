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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.Relation;

public class RelationOperationState implements Serializable
{
	private static final long serialVersionUID = 1L;
	private final List<Relation> currentRelations = new ArrayList<Relation>();
	private final List<Relation> adds = new ArrayList<Relation>();
	private final List<Relation> modifies = new ArrayList<Relation>();
	private final Set<Long> deletes = Sets.newHashSet();

	public void initForCurrent(Collection<Relation> relations)
	{
		currentRelations.addAll(relations);
	}

	public List<Relation> getAdds()
	{
		return adds;
	}

	public List<Relation> getModifies()
	{
		return modifies;
	}

	public Collection<Long> getDeletes()
	{
		return deletes;
	}

	public void add(ItemId itemId, String type, String resourceId)
	{

		Relation relation = createRelation(itemId, type, resourceId);
		adds.add(relation);
	}

	private Relation createRelation(ItemId itemId, String type, String resourceId)
	{
		Relation relation = new Relation();
		Item relatedItem = new Item();
		relatedItem.setUuid(itemId.getUuid());
		relatedItem.setVersion(itemId.getVersion());
		relation.setSecondItem(relatedItem);
		relation.setFirstResource(resourceId);
		relation.setRelationType(type);
		return relation;
	}

	public void deleteAll()
	{
		adds.clear();
		modifies.clear();
		deletes.clear();
		for( Relation relation : currentRelations )
		{
			deletes.add(relation.getId());
		}
	}

	public void deleteByResourceId(String resourceId)
	{
		Iterator<Relation> it = adds.iterator();
		while( it.hasNext() )
		{
			Relation rel = it.next();
			String firstResource = rel.getFirstResource();
			if( firstResource != null && firstResource.equals(resourceId) )
			{
				it.remove();
			}
		}

		for( Relation relation : currentRelations )
		{
			if( resourceId.equals(relation.getFirstResource()) )
			{
				deletes.add(relation.getId());
			}
		}
	}

	public void deleteByType(String relationType)
	{
		Iterator<Relation> it = adds.iterator();
		while( it.hasNext() )
		{
			Relation rel = it.next();
			if( rel.getRelationType().equals(relationType) )
			{
				it.remove();
			}
		}

		for( Relation relation : currentRelations )
		{
			if( relationType.equals(relation.getRelationType()) )
			{
				deletes.add(relation.getId());
			}
		}

	}
}
