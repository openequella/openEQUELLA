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

package com.tle.core.hierarchy.convert;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.thoughtworks.xstream.XStream;
import com.tle.beans.hierarchy.HierarchyTopic;
import com.tle.beans.item.Item;
import com.tle.common.beans.xml.IdOnlyConverter;
import com.tle.core.entity.convert.BaseEntityTreeNodeConverter;
import com.tle.core.guice.Bind;
import com.tle.core.hierarchy.HierarchyDao;
import com.tle.core.institution.convert.ConverterParams;

@Bind
@Singleton
public class HierarchyConverter extends BaseEntityTreeNodeConverter<HierarchyTopic>
{
	@Inject
	private HierarchyDao hierarchyDao;

	@SuppressWarnings("nls")
	public HierarchyConverter()
	{
		super("hierarchy", "hierarchy.xml");
	}

	@Override
	public HierarchyDao getDao()
	{
		return hierarchyDao;
	}

	@Override
	protected Map<Long, Long> getIdMap(ConverterParams params)
	{
		return params.getHierarchies();
	}

	@Override
	public ConverterId getConverterId()
	{
		return ConverterId.HIERARCHY;
	}

	@Override
	protected void preInsert(HierarchyTopic topic, ConverterParams params)
	{
		List<Item> keyResources = topic.getKeyResources();
		if( keyResources != null )
		{
			Iterator<Item> keyIter = keyResources.iterator();
			while( keyIter.hasNext() )
			{
				Item item = keyIter.next();
				Long newId = params.getItems().get(item.getId());
				if( newId != null )
				{
					item.setId(newId);
				}
				else
				{
					keyIter.remove();
				}
			}
		}
	}

	@Override
	protected void preExport(HierarchyTopic topic, ConverterParams params)
	{
		if( params.hasFlag(ConverterParams.NO_ITEMS) )
		{
			topic.setKeyResources(null);
		}
	}

	@Override
	public Class<HierarchyTopic> getNodeClass()
	{
		return HierarchyTopic.class;
	}

	@Override
	protected XStream createXStream()
	{
		XStream x = super.createXStream();
		x.registerConverter(new IdOnlyConverter(Item.class));
		return x;
	}
}
