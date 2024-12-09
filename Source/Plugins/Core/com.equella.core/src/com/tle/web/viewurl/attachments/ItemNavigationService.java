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

package com.tle.web.viewurl.attachments;

import com.tle.beans.item.Item;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.beans.item.attachments.ItemNavigationNode;
import java.util.List;

public interface ItemNavigationService {
  /**
   * @param item The item to add nodes to
   * @param existing Uses this to determine the next index to begin inserting new navigation nodes
   *     at
   * @param attachments The attachments you wish to build nodes for
   * @param nodeAdded An optional call-back for each ItemNavigationNode that is added by this method
   * @return The last index that was inserted. Not overly useful.
   */
  void populateTreeNavigationFromAttachments(
      Item item,
      List<ItemNavigationNode> existing,
      List<? extends IAttachment> attachments,
      NodeAddedCallback nodeAdded);

  interface NodeAddedCallback {
    void execute(int overallIndex, ItemNavigationNode node);
  }
}
