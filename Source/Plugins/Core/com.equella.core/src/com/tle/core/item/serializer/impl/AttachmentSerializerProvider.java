/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0, (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.CustomAttachment;
import com.tle.core.cloudproviders.CloudAttachmentSerializer;
import com.tle.core.guice.Bind;
import com.tle.core.item.dao.ItemDao;
import com.tle.core.item.security.ItemSecurityConstants;
import com.tle.core.item.serializer.AttachmentSerializer;
import com.tle.core.item.serializer.ItemSerializerProvider;
import com.tle.core.item.serializer.ItemSerializerService.SerialisationCategory;
import com.tle.core.item.serializer.ItemSerializerState;
import com.tle.core.item.serializer.XMLStreamer;
import com.tle.core.jackson.MapperExtension;
import com.tle.core.plugins.PluginTracker;
import com.tle.web.api.item.equella.interfaces.beans.BrokenAttachmentBean;
import com.tle.web.api.item.equella.interfaces.beans.EquellaAttachmentBean;
import com.tle.web.api.item.equella.interfaces.beans.EquellaItemBean;
import com.tle.web.api.item.interfaces.beans.AttachmentBean;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Bind
@Singleton
@SuppressWarnings("nls")
public class AttachmentSerializerProvider implements ItemSerializerProvider, MapperExtension {
  private static final String ALIAS_ATTACHMENTS = "attachments";

  @Inject private PluginTracker<AttachmentSerializer> tracker;
  @Inject private ItemDao itemDao;

  private Map<String, AttachmentSerializer> serializerMap;

  private Logger LOGGER = LoggerFactory.getLogger(AttachmentSerializerProvider.class);

  /**
   * Serializes the given {@link Attachment} into a concrete subclass of {@link
   * EquellaAttachmentBean}, using an appropriate serializer based on the attachment type.
   *
   * <p>If serialization fails, a {@link BrokenAttachmentBean} is returned instead.
   */
  public EquellaAttachmentBean serializeAttachment(Attachment attachment) {
    String type = attachment.getAttachmentType().name().toLowerCase();
    String uuid = attachment.getUuid();
    String desc = attachment.getDescription();
    if (type.equals("custom")) {
      type = type + '/' + ((CustomAttachment) attachment).getType();
    }

    try {
      Map<String, AttachmentSerializer> typeMap = getAttachmentSerializers();
      AttachmentSerializer attachmentSerializer = typeMap.get(type);
      if (attachmentSerializer == null) {
        throw new RuntimeException("No attachment serializer for type '" + type + "'");
      }
      EquellaAttachmentBean attachBean = attachmentSerializer.serialize(attachment);
      attachBean.setRestricted(attachment.isRestricted());
      attachBean.setPreview(attachment.isPreview());
      if (attachBean.getThumbnail() == null) {
        attachBean.setThumbnail(attachment.getThumbnail());
      }

      if (attachBean.getUuid() == null) {
        attachBean.setUuid(uuid);
      }
      if (attachBean.getDescription() == null) {
        attachBean.setDescription(desc);
      }
      if (attachBean.getViewer() == null) {
        attachBean.setViewer(attachment.getViewer());
      }
      return attachBean;
    } catch (RuntimeException e) {
      LOGGER.error("Failed to serialise attachment " + uuid, e);
      return new BrokenAttachmentBean(uuid, type, desc);
    }
  }

  @Override
  public void prepareItemQuery(ItemSerializerState state) {
    if (state.hasCategory(SerialisationCategory.ATTACHMENT)) {
      state.addPrivilege(ItemSecurityConstants.VIEW_ITEM);
    }
  }

  @Override
  public void performAdditionalQueries(ItemSerializerState state) {
    if (state.hasCategory(SerialisationCategory.ATTACHMENT)) {
      Multimap<Long, Attachment> attachments =
          itemDao.getAttachmentsForItemIds(
              state.getItemIdsWithPrivilege(ItemSecurityConstants.VIEW_ITEM));
      for (Long itemId : attachments.keySet()) {
        state.setData(itemId, ALIAS_ATTACHMENTS, attachments.get(itemId));
      }
    }
  }

  @Override
  public void writeXmlResult(XMLStreamer xml, ItemSerializerState state, long itemId) {
    if (state.hasCategory(SerialisationCategory.ATTACHMENT)) {
      throw new UnsupportedOperationException();
    }
  }

  @Override
  public void writeItemBeanResult(
      EquellaItemBean equellaItemBean, ItemSerializerState state, long itemId) {
    if (state.hasCategory(SerialisationCategory.ATTACHMENT)
        && state.hasPrivilege(itemId, ItemSecurityConstants.VIEW_ITEM)) {
      Collection<Attachment> attachments = state.getData(itemId, ALIAS_ATTACHMENTS);
      List<AttachmentBean> attachmentBeans = Lists.newArrayList();
      if (attachments != null) {
        for (Attachment attachment : attachments) {
          attachmentBeans.add(serializeAttachment(attachment));
        }
      }
      equellaItemBean.setAttachments(attachmentBeans);
    }
  }

  @Override
  public void extendMapper(ObjectMapper mapper) {
    Collection<AttachmentSerializer> serializers = getAttachmentSerializers().values();
    for (AttachmentSerializer attachmentSerializer : serializers) {
      Map<String, Class<? extends EquellaAttachmentBean>> types =
          attachmentSerializer.getAttachmentBeanTypes();
      if (types != null) {
        for (Entry<String, Class<? extends EquellaAttachmentBean>> entry : types.entrySet()) {
          mapper.registerSubtypes(new NamedType(entry.getValue(), entry.getKey()));
        }
      }
    }
  }

  public boolean exportable(EquellaAttachmentBean attachmentBean) {
    AttachmentSerializer attachmentSerializer =
        getAttachmentSerializers().get(attachmentBean.getRawAttachmentType());
    return attachmentSerializer.exportable(attachmentBean);
  }

  public synchronized Map<String, AttachmentSerializer> getAttachmentSerializers() {
    if (serializerMap == null) {
      serializerMap = tracker.getNewBeanMap();
      serializerMap.put("custom/cloud", new CloudAttachmentSerializer());
    }
    return serializerMap;
  }
}
