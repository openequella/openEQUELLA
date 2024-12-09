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

package com.tle.web.htmleditor.tinymce.addon.tle.selection.callback;

import com.tle.beans.item.ItemId;
import com.tle.beans.item.attachments.Attachment;
import com.tle.common.util.UnmodifiableIterable;
import com.tle.core.guice.Bind;
import com.tle.mypages.service.MyPagesService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.selection.SelectedResource;
import com.tle.web.selection.SelectedResourceKey;
import com.tle.web.viewable.ViewableItem;
import com.tle.web.viewable.impl.ViewableItemFactory;
import com.tle.web.viewurl.ViewableResource;
import com.tle.web.viewurl.attachments.AttachmentResourceService;
import javax.inject.Inject;
import javax.inject.Singleton;

@Bind
@Singleton
public class ScrapbookEmbedderCallback extends AbstractSelectionsMadeCallback {
  private static final long serialVersionUID = 1L;

  @Inject private ViewableItemFactory viewableItemFactory;
  @Inject private AttachmentResourceService attachmentResourceService;
  @Inject private MyPagesService myPagesService;

  @Override
  protected ViewableResource createViewableResourceFromSelection(
      SectionInfo info, SelectedResource res, String sessionId, String pageId) {
    final SelectedResourceKey key = res.getKey();
    final ItemId itemId = new ItemId(key.getUuid(), key.getVersion());
    final ViewableItem viewableItem = viewableItemFactory.createNewViewableItem(itemId);
    final UnmodifiableIterable<Attachment> attachments =
        viewableItem.getItem().getAttachmentsUnmodifiable();
    final Attachment attachment = attachments.get(0);
    final ViewableResource vres =
        attachmentResourceService.getViewableResource(info, viewableItem, attachment);
    return myPagesService.cloneMyContent(info, vres, sessionId, pageId);
  }
}
