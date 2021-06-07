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
import com.tle.beans.item.ItemId;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.mime.MimeEntry;
import com.tle.core.TextExtracterExtension;
import com.tle.web.controls.resource.ResourceAttachmentBean;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@NonNullByDefault
public interface MimeTypeService {
  @Nullable
  MimeEntry getEntryForFilename(String filename);

  /**
   * @param filename
   * @return If the extension is not known then application/octet-stream is returned
   */
  String getMimeTypeForFilename(String filename);

  String getMimeTypeForAttachmentUuid(ItemId key, String attachmentUuid);

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

  /**
   * Return a list of enabled viewers. If the default viewer does not exist in the list, add it to
   * the list. However, if the default viewer is "file", it will not be included because "file" is
   * added to the list dynamically by {@link com.tle.web.mimetypes.section.MimeDefaultViewerSection}
   *
   * @param attributes Configured attributes of MIME type
   * @return A string representing a list of enabled viewers and formatted as a JSON array of
   *     strings (e.g. ["fancy", "toimg"])
   */
  String getEnabledViewerList(Map<String, String> attributes);

  interface MimeEntryChanges {
    void editMimeEntry(MimeEntry entry);
  }
}
