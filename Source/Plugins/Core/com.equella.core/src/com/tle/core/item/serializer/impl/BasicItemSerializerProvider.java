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

package com.tle.core.item.serializer.impl;

import javax.inject.Singleton;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;

import com.tle.common.Check;
import com.tle.common.interfaces.SimpleI18NString;
import com.tle.core.guice.Bind;
import com.tle.core.item.serializer.ItemSerializerProvider;
import com.tle.core.item.serializer.ItemSerializerService;
import com.tle.core.item.serializer.ItemSerializerState;
import com.tle.core.item.serializer.XMLStreamer;
import com.tle.web.api.item.equella.interfaces.beans.EquellaItemBean;

@Bind
@Singleton
@SuppressWarnings("nls")
public class BasicItemSerializerProvider implements ItemSerializerProvider
{
	// Aliases can't be same as property name because of bug in hibernate 3.5:
	//
	// http://opensource.atlassian.com/projects/hibernate/browse/HHH-817
	//
	private static final String NAME_ALIAS = "name";
	private static final String DESC_ALIAS = "desc";
	private static final String METADATA_ALIAS = "metadata";

	@Override
	public void prepareItemQuery(ItemSerializerState state)
	{
		final DetachedCriteria criteria = state.getItemQuery();
		final ProjectionList projection = state.getItemProjection();

		if( state.hasCategory(ItemSerializerService.CATEGORY_BASIC) )
		{
			projection.add(Projections.property("name.id"), NAME_ALIAS);
			projection.add(Projections.property("description.id"), DESC_ALIAS);
		}

		if( state.hasCategory(ItemSerializerService.CATEGORY_METADATA) )
		{
			criteria.createAlias("itemXml", "itemXml");
			projection.add(Projections.property("itemXml.xml"), METADATA_ALIAS);
		}
	}

	@Override
	public void performAdditionalQueries(ItemSerializerState state)
	{
		if( state.hasCategory(ItemSerializerService.CATEGORY_BASIC) )
		{
			for( Long itemKey : state.getItemKeys() )
			{
				state.addBundleToResolve(itemKey, NAME_ALIAS);
				state.addBundleToResolve(itemKey, DESC_ALIAS);
			}
		}
	}

	@Override
	public void writeItemBeanResult(EquellaItemBean equellaItemBean, ItemSerializerState state, long itemId)
	{
		if( state.hasCategory(ItemSerializerService.CATEGORY_BASIC) )
		{
			String name = state.getResolvedBundle(itemId, NAME_ALIAS);
			if( !Check.isEmpty(name) )
			{
				equellaItemBean.setName(new SimpleI18NString(name));
			}

			String desc = state.getResolvedBundle(itemId, DESC_ALIAS);

			if( !Check.isEmpty(desc) )
			{
				equellaItemBean.setDescription(new SimpleI18NString(desc));
			}
		}

		if( state.hasCategory(ItemSerializerService.CATEGORY_METADATA) )
		{
			equellaItemBean.setMetadata((String) state.getData(itemId, METADATA_ALIAS));
		}
	}

	@Override
	public void writeXmlResult(XMLStreamer xml, ItemSerializerState state, long itemId)
	{
		if( state.hasCategory(ItemSerializerService.CATEGORY_BASIC) )
		{
			String name = state.getResolvedBundle(itemId, NAME_ALIAS);
			if( !Check.isEmpty(name) )
			{
				xml.startElement("name");
				xml.writeData(name);
				xml.endElement();
			}

			String desc = state.getResolvedBundle(itemId, DESC_ALIAS);
			if( !Check.isEmpty(desc) )
			{
				xml.startElement("description");
				xml.writeData(desc);
				xml.endElement();
			}
		}

		if( state.hasCategory(ItemSerializerService.CATEGORY_METADATA) )
		{
			xml.startElement("metadata");
			xml.writeRawXmlString((String) state.getData(itemId, METADATA_ALIAS));
			xml.endElement();
		}
	}
}
