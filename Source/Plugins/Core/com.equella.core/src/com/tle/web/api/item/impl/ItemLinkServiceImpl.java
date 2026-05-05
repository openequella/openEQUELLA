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

package com.tle.web.api.item.impl;

import com.tle.beans.item.ItemId;
import com.tle.beans.item.ItemKey;
import com.tle.core.guice.Bind;
import com.tle.core.institution.InstitutionService;
import com.tle.web.api.item.ItemLinkService;
import com.tle.web.api.item.equella.interfaces.beans.EquellaItemBean;
import com.tle.web.api.item.interfaces.beans.AttachmentBean;
import com.tle.web.api.item.interfaces.beans.ItemBean;
import com.tle.web.viewable.ViewItemLinkFactory;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;

@Bind(ItemLinkService.class)
@Singleton
@SuppressWarnings("nls")
public class ItemLinkServiceImpl implements ItemLinkService {
  private static final String PATH_ITEMAPI = "api/item/";

  public static final String REL_SELF = "self";
  public static final String REL_VIEW = "view";
  public static final String REL_THUMB = "thumbnail";
  public static final String LINKS_KEY = "links";

  @Inject private ViewItemLinkFactory linkFactory;
  @Inject private InstitutionService institutionService;

  @Override
  public URI getItemURI(ItemKey itemKey) {
    try {
      return new URI(getItemURLStr(itemKey));
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

  private String getItemURLStr(ItemKey itemKey) {
    ItemId itemId = ItemId.fromKey(itemKey);
    return institutionService.institutionalise(PATH_ITEMAPI + itemId + '/');
  }

  @Override
  public ItemBean addLinks(ItemBean itemBean) {
    return populateItemLinks(itemBean);
  }

  @Override
  public EquellaItemBean addLinks(EquellaItemBean itemBean) {
    return populateItemLinks(itemBean);
  }

  private <T extends ItemBean> T populateItemLinks(T itemBean) {
    final ItemId itemId = new ItemId(itemBean.getUuid(), itemBean.getVersion());

    itemBean.set(LINKS_KEY, buildItemLinks(itemId));
    processAttachments(itemId, itemBean.getAttachments());

    return itemBean;
  }

  private Map<String, String> buildItemLinks(ItemId itemId) {
    Map<String, String> links =
        Map.of(
            REL_SELF, getItemURLStr(itemId),
            REL_VIEW, linkFactory.createViewLink(itemId).getHref());
    return new HashMap<>(links);
  }

  private void processAttachments(ItemId itemId, List<AttachmentBean> attachments) {
    Optional.ofNullable(attachments).stream()
        .flatMap(List::stream)
        .forEach(attachment -> populateAttachmentLinks(itemId, attachment));
  }

  private void populateAttachmentLinks(ItemId itemId, AttachmentBean attachmentBean) {
    String uuid = attachmentBean.getUuid();
    Map<String, String> attachLinks =
        Map.of(
            REL_VIEW, linkFactory.createViewAttachmentLink(itemId, uuid).getHref(),
            REL_THUMB, linkFactory.createThumbnailAttachmentLink(itemId, uuid).getHref());

    attachmentBean.set(LINKS_KEY, new HashMap<>(attachLinks));
  }
}
