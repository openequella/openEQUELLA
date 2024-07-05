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

package com.tle.core.scheduler.standard.task;

import com.google.inject.Inject;
import com.tle.beans.Institution;
import com.tle.common.institution.CurrentInstitution;
import com.tle.core.guice.Bind;
import com.tle.core.hierarchy.HierarchyDao;
import com.tle.core.item.dao.ItemDao;
import com.tle.core.scheduler.ScheduledTask;
import javax.inject.Singleton;
import org.springframework.transaction.annotation.Transactional;

/**
 * Scheduled task running weekly to remove all the dynamic key resources that reference to deleted
 * Items from the current Institution.
 */
@Bind
@Singleton
public class RemoveInvalidDynamicKeyResource implements ScheduledTask {
  @Inject private ItemDao itemDao;
  @Inject private HierarchyDao hierarchyDao;

  @Transactional
  @Override
  public void execute() {
    Institution currentInstitution = CurrentInstitution.get();

    hierarchyDao
        .getAllKeyResources(currentInstitution)
        .forEach(
            keyResource -> {
              String itemUuid = keyResource.getItemUuid();
              int itemVersion = keyResource.getItemVersion();
              if (itemDao.getItemInfo(itemUuid, itemVersion) == null) {
                hierarchyDao.deleteKeyResources(itemUuid, itemVersion, currentInstitution);
              }
            });
  }
}
