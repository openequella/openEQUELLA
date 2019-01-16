/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.web.viewurl;

import java.util.Set;

import com.dytech.devlib.PropBagEx;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemKey;
import com.tle.beans.item.attachments.Attachments;
import com.tle.beans.workflow.WorkflowStatus;
import com.tle.core.item.operations.WorkflowOperation;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.TreeIndexed;
import com.tle.web.viewable.ViewableItem;

@NonNullByDefault
public interface ItemSectionInfo {
  ItemKey getItemId();

  String getItemdir();

  Item getItem();

  PropBagEx getItemxml();

  ItemDefinition getItemdef();

  @Nullable
  WorkflowStatus getWorkflowStatus();

  Set<String> getPrivileges();

  boolean hasPrivilege(String privilege);

  Attachments getAttachments();

  void modify(WorkflowOperation... ops);

  void refreshItem(boolean modified);

  @Nullable
  ViewableItem<Item> getViewableItem();

  @TreeIndexed
  interface ItemSectionInfoFactory extends SectionId {
    ItemSectionInfo getItemSectionInfo(SectionInfo info);
  }

  boolean isEditing();
}
