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

package com.tle.core.item.standard.operations;

import com.tle.beans.item.Item;
import com.tle.beans.item.ItemStatus;
import com.tle.beans.item.ModerationStatus;
import com.tle.core.item.standard.operations.workflow.TaskOperation;
import com.tle.core.security.impl.SecureItemStatus;
import com.tle.core.security.impl.SecureOnCall;

@SecureOnCall(priv = "DELETE_ITEM")
@SecureItemStatus(value = ItemStatus.DELETED, not = true)
public class DeleteOperation extends TaskOperation {
  @Override
  public boolean execute() {
    ModerationStatus moderationStatus = getModerationStatus();
    moderationStatus.setDeletedStatus(getItemStatus());
    Item item = getItem();
    moderationStatus.setDeletedModerating(item.isModerating());
    setState(ItemStatus.DELETED);
    exitTasksForItem();
    item.setModerating(false);
    return true;
  }

  @Override
  public boolean isDeleteLike() {
    return true;
  }
}
