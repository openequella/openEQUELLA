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

package com.tle.core.item.convert;

import com.tle.beans.Institution;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.ItemKey;
import com.tle.beans.viewcount.ViewcountAttachment;
import com.tle.beans.viewcount.ViewcountItem;
import com.tle.common.NameValue;
import com.tle.common.filesystem.handle.BucketFile;
import com.tle.common.filesystem.handle.SubTemporaryFile;
import com.tle.common.filesystem.handle.TemporaryFileHandle;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.ConverterParams;
import com.tle.core.institution.convert.service.AbstractJsonConverter;
import com.tle.core.institution.convert.service.InstitutionImportService.ConvertType;
import com.tle.core.institution.convert.service.impl.InstitutionImportServiceImpl.ConverterTasks;
import com.tle.core.viewcount.service.ViewCountService;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;

@Bind
@Singleton
public class ViewsConverter extends AbstractJsonConverter<Object> {
  private static final String VIEWS_FOLDER = "item_views";

  @Inject private ViewCountService viewCountService;

  @Override
  public void doDelete(Institution institution, ConverterParams callback) {
    // Handled by the itemDao
  }

  @Override
  public void doExport(
      TemporaryFileHandle staging, Institution institution, ConverterParams callback)
      throws IOException {
    final SubTemporaryFile viewsExportFolder = new SubTemporaryFile(staging, VIEWS_FOLDER);
    xmlHelper.writeExportFormatXmlFile(viewsExportFolder, true);

    for (ViewcountItem itemViewCount : viewCountService.getItemViewCountList(institution)) {
      final String uuid = itemViewCount.getId().getItemUuid();
      final int version = itemViewCount.getId().getItemVersion();

      final ItemViewsExport ive = new ItemViewsExport();
      ive.itemUuid = uuid;
      ive.itemVersion = version;
      ive.count = itemViewCount.getCount();
      ive.lastViewed = itemViewCount.getLastViewed().toEpochMilli();

      for (ViewcountAttachment viewcountAttachment :
          viewCountService.getAttachmentViewCountList(institution, new ItemId(uuid, version))) {
        final AttachmentViewsExport ave = new AttachmentViewsExport();
        ave.attachmentUuid = viewcountAttachment.getId().getAttachment();
        ave.count = viewcountAttachment.getCount();
        ave.lastViewed = viewcountAttachment.getLastViewed().toEpochMilli();

        ive.attachments.add(ave);
      }

      final BucketFile bucketFolder = new BucketFile(viewsExportFolder, uuid);
      json.write(bucketFolder, uuid + ".json", ive);
    }
  }

  @Override
  public void doImport(TemporaryFileHandle staging, Institution institution, ConverterParams params)
      throws IOException {
    final SubTemporaryFile viewsImportFolder = new SubTemporaryFile(staging, VIEWS_FOLDER);
    final List<String> entries = json.getFileList(viewsImportFolder);
    for (String entry : entries) {
      final ItemViewsExport views = json.read(viewsImportFolder, entry, ItemViewsExport.class);
      if (views != null) {
        ItemKey itemKey = new ItemId(views.itemUuid, views.itemVersion);
        viewCountService.setItemViewCount(
            itemKey, views.count, Instant.ofEpochMilli(views.lastViewed));

        for (AttachmentViewsExport attachment : views.attachments) {
          viewCountService.setAttachmentViewCount(
              itemKey,
              attachment.attachmentUuid,
              attachment.count,
              Instant.ofEpochMilli(attachment.lastViewed));
        }
      }
    }
  }

  @Override
  public void addTasks(ConvertType type, ConverterTasks tasks, ConverterParams params) {
    if (!params.hasFlag(ConverterParams.NO_ITEMS)) {
      if (!(type == ConvertType.DELETE)) {
        tasks.add(new NameValue("Item Views", "item_views"));
      }
    }
  }

  public static class ItemViewsExport {
    public String itemUuid;
    public int itemVersion;
    public int count;
    public long lastViewed;
    public List<AttachmentViewsExport> attachments = new ArrayList<>();
  }

  public static class AttachmentViewsExport {
    public String attachmentUuid;
    public int count;
    public long lastViewed;
  }
}
