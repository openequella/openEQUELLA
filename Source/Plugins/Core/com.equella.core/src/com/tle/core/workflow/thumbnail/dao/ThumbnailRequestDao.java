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

package com.tle.core.workflow.thumbnail.dao;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.Institution;
import com.tle.beans.item.ItemKey;
import com.tle.core.hibernate.dao.GenericInstitutionalDao;
import com.tle.core.item.dao.ItemDaoExtension;
import com.tle.core.workflow.thumbnail.entity.ThumbnailRequest;
import java.util.List;

@NonNullByDefault
public interface ThumbnailRequestDao
    extends GenericInstitutionalDao<ThumbnailRequest, Long>, ItemDaoExtension {
  List<ThumbnailRequest> list(Institution institution);

  List<ThumbnailRequest> list(Institution institution, ItemKey itemId);

  List<ThumbnailRequest> listForFile(Institution institution, ItemKey itemId, String filenameHash);

  List<ThumbnailRequest> listForHandle(
      Institution institution, ItemKey itemId, String serialHandle);

  @Nullable
  ThumbnailRequest getByUuid(String requestUuid);

  boolean exists(ItemKey itemId, String serialHandle, String filename, String filenameHash);
}
