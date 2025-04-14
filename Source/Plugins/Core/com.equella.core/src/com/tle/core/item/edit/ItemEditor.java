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

package com.tle.core.item.edit;

import com.dytech.devlib.PropBagEx;
import com.tle.beans.item.ItemIdKey;
import com.tle.beans.item.attachments.Attachment;
import com.tle.common.filesystem.handle.FileHandle;
import com.tle.core.item.edit.attachment.AttachmentEditor;
import com.tle.web.api.item.equella.interfaces.beans.EquellaItemBean;
import java.util.Date;
import java.util.List;
import java.util.Set;

public interface ItemEditor {
  /**
   * Asks each ItemDeserializerEditor to do edits
   *
   * @param itemBean
   */
  void doEdits(EquellaItemBean itemBean);

  Attachment attachmentForUuid(String uuid);

  /**
   * Also asks each ItemDeserializerEditor to do processFiles
   *
   * @param ensureOnIndexList
   * @return
   */
  ItemIdKey finishedEditing(boolean ensureOnIndexList);

  PropBagEx getMetadata();

  void preventSaveScript();

  void editDates(Date dateCreated, Date dateModified);

  void editItemStatus(String status);

  void editOwner(String owner);

  void editCollaborators(Set<String> collaborators);

  void editRating(Float rating);

  void editMetadata(String xml);

  void editMetadata(PropBagEx xml);

  void editThumbnail(String thumbnail);

  <T extends AttachmentEditor> T getAttachmentEditor(String uuid, Class<T> type);

  Iterable<String> getAttachmentOrder();

  void editAttachmentOrder(List<String> attachmentUuids);

  void processExportDetails(EquellaItemBean itemBean);

  NavigationEditor getNavigationEditor();

  DRMEditor getDRMEditor();

  void unlock();

  FileHandle getFileHandle();

  boolean isNewItem();
}
