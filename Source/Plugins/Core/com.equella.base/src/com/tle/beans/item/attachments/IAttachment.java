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

package com.tle.beans.item.attachments;

import com.tle.beans.item.IItem;
import java.util.Map;

public interface IAttachment {
  String getDescription();

  void setDescription(String description);

  String getUrl();

  void setUrl(String url);

  void setData(String name, Object value);

  Object getData(String name);

  Map<String, Object> getDataAttributesReadOnly();

  Map<String, Object> getDataAttributes();

  void setDataAttributes(Map<String, Object> data);

  AttachmentType getAttachmentType();

  String getUuid();

  void setUuid(String uuid);

  String getThumbnail();

  void setThumbnail(String thumbnail);

  String getMd5sum();

  void setMd5sum(String md5sum);

  String getViewer();

  void setViewer(String viewer);

  boolean isPreview();

  void setPreview(boolean preview);

  void setRestricted(boolean restricted);

  boolean isRestricted();

  IItem<?> getItem();

  /**
   * This value gets set to true when an attachment attempts to index and times out, which indicates
   * a problem with the file. Used to avoid instability issues caused by attempting to index a
   * broken file indefinitely.
   *
   * <p>The timeout is controlled by textExtracter.parseDurationCap in milliseconds in the
   * plugins/com.tle.core.freetext/optional.properties file. The default is 60000.
   *
   * <p>Stored in the attachment table in the errored_when_indexing column. Column was initially
   * added via com.tle.core.institution.migration.v20202.AddIndexingErrorColumnMigration.
   *
   * @see com.tle.core.institution.migration.v20202
   * @return Returns true if the attachment failed to index previously
   */
  boolean isErroredIndexing();

  /**
   * Sets the value of erroredIndexing. If set to true and updated in the database, then the
   * attachment will be skipped for indexing in future.
   *
   * @param erroredIndexing The value to set erroredIndexing to
   */
  void setErroredIndexing(boolean erroredIndexing);
}
