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

package com.tle.web.api.baseentity.serializer;

import com.dytech.edge.common.LockedException;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.entity.BaseEntity;
import com.tle.common.beans.exception.InvalidDataException;
import com.tle.exceptions.AccessDeniedException;
import com.tle.web.api.interfaces.beans.BaseEntityBean;

@NonNullByDefault
public interface BaseEntitySerializer<BE extends BaseEntity, BEB extends BaseEntityBean> {
  BEB serialize(BE entity, @Nullable Object data, boolean heavy);

  @Nullable
  BE deserializeEdit(
      @Nullable String uuid,
      BEB bean,
      @Nullable String stagingUuid,
      @Nullable String lockId,
      boolean keepLocked,
      boolean importing)
      throws LockedException, AccessDeniedException, InvalidDataException;

  BE deserializeNew(BEB bean, @Nullable String stagingUuid, boolean importing)
      throws AccessDeniedException, InvalidDataException;
}
