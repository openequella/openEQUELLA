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

package com.tle.core.item.operations;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemPack;

@NonNullByDefault
public interface WorkflowOperation {
  /**
   * @return true if you modified the item
   */
  boolean execute();

  @Nullable
  Item getItem();

  @Nullable
  ItemPack<Item> getItemPack();

  boolean isReadOnly();

  void setParams(ItemOperationParams params);

  boolean failedToAutowire();

  /**
   * I.e. purge or delete
   *
   * @return
   */
  boolean isDeleteLike();
}
