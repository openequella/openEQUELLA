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

package com.tle.mets.importerexporters;

import com.dytech.devlib.PropBagEx;
import com.google.common.base.Strings;
import com.tle.beans.item.Item;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.AttachmentType;
import com.tle.beans.item.attachments.ItemNavigationNode;
import com.tle.beans.item.attachments.LinkAttachment;
import com.tle.common.filesystem.handle.FileHandle;
import com.tle.core.guice.Bind;
import com.tle.mets.MetsIDElementInfo;
import com.tle.web.sections.SectionInfo;
import edu.harvard.hul.ois.mets.FLocat;
import edu.harvard.hul.ois.mets.File;
import edu.harvard.hul.ois.mets.Loctype;
import edu.harvard.hul.ois.mets.helper.MetsElement;
import edu.harvard.hul.ois.mets.helper.MetsIDElement;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Singleton;

@Bind
@Singleton
@SuppressWarnings("nls")
public class LinkMetsAttachmentImporterExporter extends AbstractMetsAttachmentImportExporter {
  @Override
  public boolean canExport(Item item, Attachment attachment) {
    return attachment.getAttachmentType() == AttachmentType.LINK;
  }

  @Override
  public List<MetsIDElementInfo<? extends MetsIDElement>> export(
      SectionInfo info, Item item, Attachment attachment) {
    final List<MetsIDElementInfo<? extends MetsIDElement>> res =
        new ArrayList<MetsIDElementInfo<? extends MetsIDElement>>();

    final LinkAttachment link = (LinkAttachment) attachment;

    final PropBagEx xmlData = new PropBagEx();
    xmlData.setNode("url", link.getUrl());
    xmlData.setNode("uuid", link.getUuid());
    xmlData.setNode("description", link.getDescription());

    final FLocat location = new FLocat();
    location.setID("link:" + attachment.getUuid());
    location.setXlinkHref(attachment.getUrl());
    location.setXlinkTitle(attachment.getDescription());
    location.setLOCTYPE(Loctype.URL);

    res.add(new MetsIDElementInfo<FLocat>(location, "equella/link", xmlData));
    return res;
  }

  @Override
  public boolean canImport(
      File parentElem, MetsElement elem, PropBagEx xmlData, ItemNavigationNode parentNode) {
    final boolean prefixMatch = idPrefixMatch(elem, "link:");
    final String url = xmlData.getNode("url");
    return prefixMatch && !Strings.isNullOrEmpty(url);
  }

  @Override
  public void doImport(
      Item item,
      FileHandle staging,
      String packageFile,
      File parentElem,
      MetsElement elem,
      PropBagEx xmlData,
      ItemNavigationNode parentNode,
      AttachmentAdder attachmentAdder) {
    final LinkAttachment link = new LinkAttachment();
    link.setUrl(xmlData.getNode("url"));
    link.setUuid(xmlData.getNode("uuid"));
    final String description = xmlData.getNode("description");
    link.setDescription(description);

    attachmentAdder.addAttachment(parentNode, link, description);
  }
}
