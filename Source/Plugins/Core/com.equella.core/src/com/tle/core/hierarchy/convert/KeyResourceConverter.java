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

package com.tle.core.hierarchy.convert;

import com.tle.beans.Institution;
import com.tle.beans.hierarchy.HierarchyTopicKeyResource;
import com.tle.common.filesystem.handle.SubTemporaryFile;
import com.tle.common.filesystem.handle.TemporaryFileHandle;
import com.tle.core.guice.Bind;
import com.tle.core.hierarchy.HierarchyDao;
import com.tle.core.institution.convert.AbstractMigratableConverter;
import com.tle.core.institution.convert.ConverterParams;
import java.io.IOException;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;

@Bind
@Singleton
public class KeyResourceConverter extends AbstractMigratableConverter<Object> {
  public static final String KEYRESOURCES_ID = "KEYRESOURCES";
  public static final String KEY_RESOURCES_IMPORT_EXPORT_FOLDER = "keyresources";

  @Inject private HierarchyDao hierarchyDao;

  @SuppressWarnings("nls")
  @Override
  public void doExport(TemporaryFileHandle staging, Institution institution, ConverterParams params)
      throws IOException {
    final SubTemporaryFile keyResourceExportFolder =
        new SubTemporaryFile(staging, KEY_RESOURCES_IMPORT_EXPORT_FOLDER);
    // write out the format details
    xmlHelper.writeExportFormatXmlFile(keyResourceExportFolder, true);

    hierarchyDao
        .getAllKeyResources(institution)
        .forEach(
            key -> {
              initialiserService.initialise(key);
              xmlHelper.writeXmlFile(keyResourceExportFolder, key.getId() + ".xml", key);
            });
  }

  @Override
  public void doImport(TemporaryFileHandle staging, Institution institution, ConverterParams params)
      throws IOException {
    final SubTemporaryFile keyResourceImportFolder =
        new SubTemporaryFile(staging, KEY_RESOURCES_IMPORT_EXPORT_FOLDER);
    final List<String> entries = xmlHelper.getXmlFileList(keyResourceImportFolder);

    entries.forEach(
        entry -> {
          HierarchyTopicKeyResource keyResources =
              xmlHelper.readXmlFile(keyResourceImportFolder, entry);
          keyResources.setInstitution(institution);

          hierarchyDao.saveKeyResources(keyResources);
          hierarchyDao.flush();
          hierarchyDao.clear();
        });
  }

  @Override
  public void doDelete(Institution institution, ConverterParams callback) {
    hierarchyDao.deleteAllKeyResources(institution);
  }

  @Override
  public String getStringId() {
    return KEYRESOURCES_ID;
  }
}
