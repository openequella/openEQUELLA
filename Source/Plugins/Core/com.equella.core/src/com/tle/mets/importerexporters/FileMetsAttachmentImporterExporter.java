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
import com.dytech.edge.common.FileInfo;
import com.tle.beans.item.Item;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.AttachmentType;
import com.tle.beans.item.attachments.FileAttachment;
import com.tle.beans.item.attachments.ItemNavigationNode;
import com.tle.common.filesystem.handle.FileHandle;
import com.tle.core.guice.Bind;
import com.tle.core.item.service.ItemFileService;
import com.tle.core.services.FileSystemService;
import com.tle.mets.MetsIDElementInfo;
import com.tle.web.sections.SectionInfo;
import edu.harvard.hul.ois.mets.BinData;
import edu.harvard.hul.ois.mets.File;
import edu.harvard.hul.ois.mets.helper.MetsElement;
import edu.harvard.hul.ois.mets.helper.MetsIDElement;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;

@Bind
@Singleton
@SuppressWarnings("nls")
public class FileMetsAttachmentImporterExporter extends AbstractMetsAttachmentImportExporter {
  @Inject private FileSystemService fileSystemService;
  @Inject private ItemFileService itemFileService;

  @Override
  public boolean canExport(Item item, Attachment attachment) {
    return attachment.getAttachmentType() == AttachmentType.FILE
        || attachment.getAttachmentType() == AttachmentType.ZIP;
  }

  @Override
  public List<MetsIDElementInfo<? extends MetsIDElement>> export(
      SectionInfo info, Item item, Attachment attachment) {
    final List<MetsIDElementInfo<? extends MetsIDElement>> res =
        new ArrayList<MetsIDElementInfo<? extends MetsIDElement>>();

    final FileHandle fileHandle = itemFileService.getItemFile(item);
    final String filename = attachment.getUrl();
    final FileInfo fileInfo = fileSystemService.getFileInfo(fileHandle, filename);
    res.add(
        exportBinaryFile(
            itemFileService.getItemFile(item),
            filename,
            fileInfo.getLength(),
            attachment.getDescription(),
            "data:" + attachment.getUuid(),
            attachment.getUuid()));
    return res;
  }

  @Override
  public boolean canImport(
      File parentElem, MetsElement elem, PropBagEx xmlData, ItemNavigationNode parentNode) {
    return idPrefixMatch(elem, "data:");
  }

  @Override
  public void doImport(
      Item item,
      FileHandle staging,
      String targetFolder,
      File parentElem,
      MetsElement elem,
      PropBagEx xmlData,
      ItemNavigationNode parentNode,
      AttachmentAdder attachmentAdder) {
    final BinData data = getFirst(elem.getContent(), BinData.class);
    if (data != null) {
      final ImportInfo importInfo =
          importBinaryFile(data, staging, targetFolder, parentElem.getOWNERID(), xmlData);

      final FileAttachment attachment = new FileAttachment();
      populateStandardProperties(attachment, importInfo);

      attachmentAdder.addAttachment(parentNode, attachment, importInfo.getDescription());
    }
  }
}
