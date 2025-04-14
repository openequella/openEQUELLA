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

import com.tle.beans.entity.BaseEntity;
import com.tle.web.api.interfaces.beans.BaseEntityBean;

public abstract class AbstractEquellaBaseEntitySerializer<
        BE extends BaseEntity, BEB extends BaseEntityBean, ED extends BaseEntityEditor<BE, BEB>>
    extends AbstractBaseEntitySerializer<BE, BEB, ED> {
  @Override
  protected void copyBaseEntityFields(BaseEntity source, BaseEntityBean target, boolean heavy) {
    super.copyBaseEntityFields(source, target, heavy);
    // Jolse says don't do it properly
    // target.set("systemType", source.isSystemType());
  }
}
