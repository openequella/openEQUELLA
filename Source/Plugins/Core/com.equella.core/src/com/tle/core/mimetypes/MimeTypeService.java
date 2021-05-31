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

package com.tle.core.mimetypes;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.item.ItemKey;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.mime.MimeEntry;
import com.tle.core.TextExtracterExtension;
import com.tle.web.api.item.equella.interfaces.beans.AbstractFileAttachmentBean;
import com.tle.web.controls.resource.ResourceAttachmentBean;
import java.util.Collection;
import java.util.List;

@NonNullByDefault
public interface MimeTypeService {
  @Nullable
  MimeEntry getEntryForFilename(String filename);

  /**
   * @param filename
   * @return If the extension is not known then application/octet-stream is returned
   */
  String getMimeTypeForFilename(String filename);

  /**
   * Get MIME type for a given resource attachment. If the attachment it is pointed towards doesn't
   * exist in the database, it will return dead/attachment.
   *
   * @param bean The attachment to get the MIME Type for.
   * @return MIME type of the attachment
   */
  String getMimeTypeForResourceAttachment(ResourceAttachmentBean bean);

  /**
   * Get MIME type for a given file attachment. If the attachment is not accessible from the
   * filestore, it will return dead/attachment.
   *
   * @param bean The attachment to check for a MIME type
   * @param itemKey The item key for the attachment's item, required as it gives us the location of
   *     the attachment in the filestore
   * @return MIME type of the attachment
   */
  String getMimeTypeForFileAttachment(AbstractFileAttachmentBean bean, ItemKey itemKey);

  /**
   * Get MIME type for a resource attachment pointing at an item summary. If the item is not
   * present, it will return dead/attachment. If it is, it will return equella/item.
   *
   * @param bean They attachment to check for a MIME type
   * @return MIME type of the attachment
   */
  String getMimeTypeForResourceItem(ResourceAttachmentBean bean);

  String getMimeTypeForResourceAttachmentBean(ResourceAttachmentBean resourceAttachmentBean);

  @Nullable
  MimeEntry getEntryForMimeType(String mimeType);

  MimeEntry getEntryForId(long id);

  /**
   * Implied wildcard at end of mimeType
   *
   * @param mimeType
   * @return
   */
  MimeTypesSearchResults searchByMimeType(String mimeType, int offset, int length);

  /**
   * Implied wildcard at end of mimeType
   *
   * @param filename File name or extension
   * @return
   */
  Collection<MimeEntry> searchByFilename(String filename);

  void saveOrUpdate(long mimeEntryId, MimeEntryChanges changes);

  void delete(MimeEntry mimeEntry);

  void delete(long id);

  <T> List<T> getListFromAttribute(MimeEntry entry, String key, Class<T> entryType);

  @Nullable
  <T> T getBeanFromAttribute(MimeEntry entry, String key, Class<T> entryType);

  void setListAttribute(MimeEntry entry, String key, @Nullable Collection<?> list);

  <T> void setBeanAttribute(MimeEntry entry, String key, @Nullable T bean);

  void clearAllForPrefix(MimeEntry entry, String keyPrefix);

  List<TextExtracterExtension> getAllTextExtracters();

  List<TextExtracterExtension> getTextExtractersForMimeEntry(MimeEntry mimeEntry);

  @Nullable
  String getMimeEntryForAttachment(Attachment attachment);

  interface MimeEntryChanges {
    void editMimeEntry(MimeEntry entry);
  }
}
