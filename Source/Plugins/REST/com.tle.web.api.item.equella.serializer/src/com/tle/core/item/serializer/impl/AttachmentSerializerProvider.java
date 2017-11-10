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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.CustomAttachment;
import com.tle.core.guice.Bind;
import com.tle.core.item.dao.ItemDao;
import com.tle.core.item.security.ItemSecurityConstants;
import com.tle.core.item.serializer.AttachmentSerializer;
import com.tle.core.item.serializer.ItemSerializerProvider;
import com.tle.core.item.serializer.ItemSerializerService;
import com.tle.core.item.serializer.ItemSerializerState;
import com.tle.core.item.serializer.XMLStreamer;
import com.tle.core.jackson.MapperExtension;
import com.tle.core.plugins.PluginTracker;
import com.tle.web.api.item.equella.interfaces.beans.EquellaAttachmentBean;
import com.tle.web.api.item.equella.interfaces.beans.EquellaItemBean;
import com.tle.web.api.item.interfaces.beans.AttachmentBean;

@Bind
@Singleton
@SuppressWarnings("nls")
public class AttachmentSerializerProvider implements ItemSerializerProvider, MapperExtension
{
	private static final String ALIAS_ATTACHMENTS = "attachments";

	@Inject
	private PluginTracker<AttachmentSerializer> tracker;
	@Inject
	private ItemDao itemDao;

	@Override
	public void prepareItemQuery(ItemSerializerState state)
	{
		if( state.hasCategory(ItemSerializerService.CATEGORY_ATTACHMENT) )
		{
			state.addPrivilege(ItemSecurityConstants.VIEW_ITEM);
		}
	}

	@Override
	public void performAdditionalQueries(ItemSerializerState state)
	{
		if( state.hasCategory(ItemSerializerService.CATEGORY_ATTACHMENT) )
		{
			Multimap<Long, Attachment> attachments = itemDao
				.getAttachmentsForItemIds(state.getItemIdsWithPrivilege(ItemSecurityConstants.VIEW_ITEM));
			for( Long itemId : attachments.keySet() )
			{
				state.setData(itemId, ALIAS_ATTACHMENTS, attachments.get(itemId));
			}
		}
	}

	@Override
	public void writeXmlResult(XMLStreamer xml, ItemSerializerState state, long itemId)
	{
		if( state.hasCategory(ItemSerializerService.CATEGORY_ATTACHMENT) )
		{
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public void writeItemBeanResult(EquellaItemBean equellaItemBean, ItemSerializerState state, long itemId)
	{
		if( state.hasCategory(ItemSerializerService.CATEGORY_ATTACHMENT)
			&& state.hasPrivilege(itemId, ItemSecurityConstants.VIEW_ITEM) )
		{
			Map<String, AttachmentSerializer> typeMap = tracker.getBeanMap();
			Collection<Attachment> attachments = state.getData(itemId, ALIAS_ATTACHMENTS);
			List<AttachmentBean> attachmentBeans = Lists.newArrayList();
			if( attachments != null )
			{
				for( Attachment attachment : attachments )
				{
					String type = attachment.getAttachmentType().name().toLowerCase();
					if( type.equals("custom") )
					{
						type = type + '/' + ((CustomAttachment) attachment).getType();
					}
					AttachmentSerializer attachmentSerializer = typeMap.get(type);
					if( attachmentSerializer == null )
					{
						throw new RuntimeException("No attachment serializer for type '" + type + "'");
					}
					EquellaAttachmentBean attachBean = attachmentSerializer.serialize(attachment);
					attachBean.setRestricted(attachment.isRestricted());
					attachBean.setPreview(attachment.isPreview());
					if (attachBean.getThumbnail() == null)
					{
						attachBean.setThumbnail(attachment.getThumbnail());
					}
					if( attachBean.getUuid() == null )
					{
						attachBean.setUuid(attachment.getUuid());
					}
					if( attachBean.getDescription() == null )
					{
						attachBean.setDescription(attachment.getDescription());
					}
					if( attachBean.getViewer() == null )
					{
						attachBean.setViewer(attachment.getViewer());
					}
					attachmentBeans.add(attachBean);
				}
			}
			equellaItemBean.setAttachments(attachmentBeans);
		}
	}

	@Override
	public void extendMapper(ObjectMapper mapper)
	{
		List<AttachmentSerializer> serializers = tracker.getBeanList();
		for( AttachmentSerializer attachmentSerializer : serializers )
		{
			Map<String, Class<? extends EquellaAttachmentBean>> types = attachmentSerializer.getAttachmentBeanTypes();
			if( types != null )
			{
				for( Entry<String, Class<? extends EquellaAttachmentBean>> entry : types.entrySet() )
				{
					mapper.registerSubtypes(new NamedType(entry.getValue(), entry.getKey()));
				}
			}
		}
	}

	public boolean exportable(EquellaAttachmentBean attachmentBean)
	{
		AttachmentSerializer attachmentSerializer = tracker.getBeanMap().get(attachmentBean.getRawAttachmentType());
		return attachmentSerializer.exportable(attachmentBean);
	}
}
