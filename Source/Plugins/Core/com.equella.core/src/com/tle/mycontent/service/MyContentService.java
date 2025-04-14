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

package com.tle.mycontent.service;

import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.core.item.operations.WorkflowOperation;
import com.tle.mycontent.ContentHandler;
import com.tle.mycontent.web.section.ContributeMyContentAction;
import com.tle.mycontent.web.section.MyContentContributeSection;
import com.tle.web.sections.SectionInfo;
import java.util.Set;

public interface MyContentService {
  boolean isMyContentContributionAllowed();

  ItemDefinition getMyContentItemDef();

  boolean isMyContentItem(Item item);

  /**
   * @param info
   * @return Are we able to return?
   */
  boolean returnFromContribute(SectionInfo info);

  Set<String> getContentHandlerIds();

  String getContentHandlerNameKey(String handlerId);

  ContentHandler getHandlerForId(String handlerId);

  WorkflowOperation getEditOperation(
      MyContentFields fields,
      String filename,
      String stagingUuid,
      boolean removeExistingAttachments,
      boolean useExistingAttachment);

  MyContentFields getFieldsForItem(ItemId itemId);

  void delete(ItemId itemId);

  void forwardToEditor(SectionInfo info, ItemId itemId);

  /**
   * Forward to Section {@link MyContentContributeSection} to edit a Scrapbook from New UI. The ID
   * of a New UI SearchPageOptions should be provided to make sure the SearchPageOptions will be
   * applied to the New UI initial search when the page returns to New UI.
   *
   * @param info Info of the original Section which receives the request from New UI.
   * @param itemId ID of an Item including UUID and version.
   * @param newUIStateId ID of the New UI SearchPageOptions saved in the browser storage.
   */
  void forwardToEditorFromNewUI(SectionInfo info, ItemId itemId, String newUIStateId);

  ContributeMyContentAction createActionForHandler(String handlerId);

  void forwardToContribute(SectionInfo info, String handlerId);

  /**
   * Forward to Section {@link MyContentContributeSection} to contribute a Scrapbook from New UI.
   * The ID of a New UI SearchPageOptions should be provided to make sure the SearchPageOptions will
   * be applied to the New UI initial search when the page returns to New UI.
   *
   * @param info Info of the original Section which receives the request from New UI.
   * @param handlerId ID of a Scrapbook handler for either File or Webpage.
   * @param newUIStateId ID of the New UI SearchPageOptionsID saved in the local storage of browser.
   */
  void forwardToContributeFromNewUI(SectionInfo info, String handlerId, String newUIStateId);

  void restore(ItemId itemId);
}
