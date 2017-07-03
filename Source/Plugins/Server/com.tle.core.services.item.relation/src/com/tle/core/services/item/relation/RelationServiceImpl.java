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

import javax.inject.Inject;
import javax.inject.Singleton;

import org.java.plugin.registry.Extension;
import org.java.plugin.registry.Extension.Parameter;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemKey;
import com.tle.beans.item.Relation;
import com.tle.common.beans.exception.NotFoundException;
import com.tle.core.guice.Bind;
import com.tle.core.item.service.ItemService;
import com.tle.core.plugins.PluginTracker;
import com.tle.exceptions.AccessDeniedException;

@Bind(RelationService.class)
@Singleton
@SuppressWarnings("nls")
public class RelationServiceImpl implements RelationService
{
	@Inject
	private PluginTracker<RelationListener> listenerTracker;
	@Inject
	private RelationDao dao;
	@Inject
	private ItemService itemService;

	private LoadingCache<String, List<RelationListener>> listenersForType = CacheBuilder.newBuilder().build(
		new CacheLoader<String, List<RelationListener>>()
		{
			@Override
			public List<RelationListener> load(String relationType)
			{
				List<RelationListener> listeners = Lists.newArrayList();
				List<Extension> extensions = listenerTracker.getExtensions();
				for( Extension extension : extensions )
				{
					Collection<Parameter> types = extension.getParameters("type");
					for( Parameter typeParam : types )
					{
						if( typeParam.valueAsString().equals(relationType) )
						{
							listeners.add(listenerTracker.getBeanByExtension(extension));
						}
					}
				}
				return listeners;
			}
		});

	@Override
	public Collection<Relation> getAllByFromItem(Item from)
	{
		return dao.getAllByFromItem(from);
	}

	@Override
	public Collection<Relation> getAllByToItem(Item to)
	{
		return dao.getAllByToItem(to);
	}

	@Override
	public Collection<Relation> getAllByType(String type)
	{
		return dao.getAllByType(type);
	}

	@Override
	public Collection<Relation> getAllByFromItemAndType(Item from, String type)
	{
		return dao.getAllByFromItemAndType(from, type);
	}

	@Override
	public long saveRelation(Relation relation)
	{
		return dao.save(relation);
	}

	@Override
	public void updateRelation(Relation relation)
	{
		dao.merge(relation);
	}

	@Override
	public void delete(Relation relation)
	{
		dao.delete(relation);
	}

	@Override
	public Relation getById(long id)
	{
		return dao.findById(id);
	}

	@Override
	public Collection<Relation> getAllByToItemAndType(Item to, String type)
	{
		return dao.getAllByToItemAndType(to, type);
	}

	@Override
	@Transactional
	public Relation getForView(ItemKey itemId, long relationId)
	{
		// For security and existance
		itemService.get(itemId);
		Relation relation = dao.findById(relationId);
		if( relation == null )
		{
			throw new NotFoundException("Relation '" + relationId + "' not found");
		}
		return relation;
	}

	@Override
	@Transactional
	public List<Relation> getRelationsForItem(ItemKey itemId)
	{
		// For security and existance
		itemService.get(itemId);
		return dao.getAllMentioningItem(itemId);
	}

	private List<RelationListener> getListenerByType(String relationType)
	{
		if( listenerTracker.needsUpdate() )
		{
			listenersForType.invalidateAll();
		}
		return listenersForType.getUnchecked(relationType);
	}

	@Override
	@Transactional
	public void delete(ItemKey itemId, long relationId)
	{
		Item item = itemService.getForEdit(itemId);
		Relation relation = dao.findById(relationId);
		if( relation == null )
		{
			throw new NotFoundException("Relation '" + relationId + "' not found");
		}
		if( relation.getFirstItem().getId() != item.getId() && relation.getSecondItem().getId() != item.getId() )
		{
			throw new AccessDeniedException("Item '" + itemId + "' is not part of relation '" + relationId + "'");
		}
		dao.delete(relation);
		List<RelationListener> listeners = getListenerByType(relation.getRelationType());
		for( RelationListener relationListener : listeners )
		{
			relationListener.relationDeleted(relation);
		}
	}

	@Override
	@Transactional
	public long createRelation(ItemKey fromItem, ItemKey toItem, boolean editorIsFrom, String type,
		String fromResource, String toResource)
	{
		Item item = itemService.getForEdit(editorIsFrom ? fromItem : toItem);
		Item otherItem = itemService.getUnsecure(editorIsFrom ? toItem : fromItem);
		Item firstItem = editorIsFrom ? item : otherItem;
		Item secondItem = editorIsFrom ? otherItem : item;
		Relation relation = new Relation();
		relation.setFirstItem(firstItem);
		relation.setSecondItem(secondItem);
		relation.setRelationType(type);
		relation.setFirstResource(fromResource);
		relation.setSecondResource(toResource);
		Long relationId = dao.save(relation);
		List<RelationListener> listeners = getListenerByType(type);
		for( RelationListener relationListener : listeners )
		{
			relationListener.relationCreated(relation);
		}
		return relationId;
	}

}
