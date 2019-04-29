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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Singleton;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.core.guice.Bind;
import com.tle.core.plugins.PluginTracker;
import com.tle.web.viewurl.attachments.AttachmentNode;
import com.tle.web.viewurl.attachments.AttachmentTreeExtension;
import com.tle.web.viewurl.attachments.AttachmentTreeService;
import com.tle.web.viewurl.attachments.DefaultAttachmentNode;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import org.java.plugin.registry.Extension;
import org.java.plugin.registry.Extension.Parameter;

@Bind(AttachmentTreeService.class)
@Singleton
public class AttachmentTreeServiceImpl implements AttachmentTreeService {
  @Inject private PluginTracker<AttachmentTreeExtension> attachmentTreeTracker;

  private Map<String, AttachmentTreeExtension> extensionMap;

  @Override
  public List<AttachmentNode> getTreeStructure(
      Iterable<? extends IAttachment> attachments, boolean flattenHidden) {
    List<AttachmentNode> nodes = Lists.newArrayList();
    List<IAttachment> attachList = Lists.newLinkedList(attachments);
    Map<String, AttachmentTreeExtension> extMap = getExtensionMap();
    while (attachList.size() > 0) {
      IAttachment attachment = attachList.remove(0);
      String type = AttachmentResourceServiceImpl.getTypeForAttachment(attachment);
      AttachmentTreeExtension treeExtension = extMap.get(type);
      if (treeExtension != null) {
        treeExtension.addRootAttachmentNode(attachment, attachList, nodes, flattenHidden);
      } else {
        nodes.add(new DefaultAttachmentNode(attachment));
      }
    }
    return nodes;
  }

  private synchronized Map<String, AttachmentTreeExtension> getExtensionMap() {
    if (extensionMap == null) {
      extensionMap = Maps.newHashMap();
      List<Extension> extensions = attachmentTreeTracker.getExtensions();
      for (Extension extension : extensions) {
        Collection<Parameter> typeParams = extension.getParameters("type"); // $NON-NLS-1$
        for (Parameter parameter : typeParams) {
          String typeString = parameter.valueAsString();
          extensionMap.put(typeString, attachmentTreeTracker.getBeanByExtension(extension));
        }
      }
    }
    return extensionMap;
  }
}
