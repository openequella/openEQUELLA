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

package com.tle.web.viewurl.attachments.impl;

import com.tle.beans.item.Item;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.beans.item.attachments.ItemNavigationNode;
import com.tle.beans.item.attachments.ItemNavigationTab;
import com.tle.core.guice.Bind;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.viewurl.attachments.AttachmentNode;
import com.tle.web.viewurl.attachments.AttachmentTreeService;
import com.tle.web.viewurl.attachments.ItemNavigationService;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;

@Bind(ItemNavigationService.class)
@Singleton
public class ItemNavigationServiceImpl implements ItemNavigationService {
  private static PluginResourceHelper r =
      ResourcesService.getResourceHelper(ItemNavigationServiceImpl.class);
  @Inject private AttachmentTreeService attachmentTreeService;

  @Override
  public void populateTreeNavigationFromAttachments(
      Item item,
      List<ItemNavigationNode> treeNodes,
      List<? extends IAttachment> attachments,
      NodeAddedCallback nodeAdded) {
    int index = findNextRootIndex(treeNodes);
    List<AttachmentNode> rootNodes = attachmentTreeService.getTreeStructure(attachments, true);
    addNodes(item, treeNodes, null, index, rootNodes, nodeAdded);
  }

  private void addNodes(
      Item item,
      List<ItemNavigationNode> treeNodes,
      ItemNavigationNode parent,
      int index,
      List<AttachmentNode> nodes,
      NodeAddedCallback nodeAdded) {
    for (AttachmentNode attachNode : nodes) {
      IAttachment attach = attachNode.getAttachment();
      ItemNavigationNode node = new ItemNavigationNode(item);

      treeNodes.add(node);

      String displayName = attach.getDescription();
      node.setIndex(index);
      node.setParent(parent);
      node.setName(displayName);

      List<ItemNavigationTab> tabs = new ArrayList<ItemNavigationTab>();
      ItemNavigationTab tab = new ItemNavigationTab();
      tab.setNode(node);
      tab.setName(r.getString("navigation.tabname.default")); // $NON-NLS-1$
      tab.setAttachment((Attachment) attach);
      tabs.add(tab);
      node.setTabs(tabs);

      if (nodeAdded != null) {
        nodeAdded.execute(treeNodes.size(), node);
      }
      index++;
      addNodes(item, treeNodes, node, 0, attachNode.getChildren(), nodeAdded);
    }
  }

  private int findNextRootIndex(List<ItemNavigationNode> currentNodes) {
    int index = 0;
    for (ItemNavigationNode node : currentNodes) {
      if (node.getParent() == null) {
        index++;
      }
    }
    return index;
  }
}
