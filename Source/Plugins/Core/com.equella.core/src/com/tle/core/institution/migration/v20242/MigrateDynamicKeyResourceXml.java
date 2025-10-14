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

package com.tle.core.institution.migration.v20242;

import com.dytech.devlib.PropBagEx;
import com.tle.common.filesystem.handle.SubTemporaryFile;
import com.tle.common.filesystem.handle.TemporaryFileHandle;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.ConverterParams;
import com.tle.core.institution.convert.InstitutionInfo;
import com.tle.core.institution.convert.XmlMigrator;
import javax.inject.Singleton;

/**
 * The table for dynamic key resource has been refactored (e.g. column renaming, table renaming) to
 * support normal key resources. To support the import of data exported before 2024.2, this
 * migration is created to update the XML file contents for dynamic key resources.
 *
 * <p>For example from:
 *
 * <pre>{@code
 * <com.tle.beans.hierarchy.HierarchyTopicDynamicKeyResources>
 *   <id>161887</id>
 *   <dynamicHierarchyId>886aa61d-f8df-4e82-8984-c487849f80ff:A+James</dynamicHierarchyId>
 *   <uuid>e35390cf-7c45-4f71-bb94-e6ccc1f09394</uuid>
 *   <version>1</version>
 *   <dateCreated class="sql-timestamp">2023-11-17 09:09:44.485</dateCreated>
 * </com.tle.beans.hierarchy.HierarchyTopicDynamicKeyResources>
 * }</pre>
 *
 * To:
 *
 * <pre>{@code
 * <com.tle.beans.hierarchy.HierarchyTopicKeyResource>
 *   <id>161887</id>
 *   <hierarchyCompoundUuid>886aa61d-f8df-4e82-8984-c487849f80ff:A+James</hierarchyCompoundUuid>
 *   <itemUuid>e35390cf-7c45-4f71-bb94-e6ccc1f09394</itemUuid>
 *   <itemVersion>1</itemVersion>
 *   <dateCreated class="sql-timestamp">2023-11-17 09:09:44.485</dateCreated>
 * </com.tle.beans.hierarchy.HierarchyTopicKeyResource>
 * }</pre>
 */
@Bind
@Singleton
public class MigrateDynamicKeyResourceXml extends XmlMigrator {
  private static final String LEGACY_FOLDER = "dynakeyresources";
  private static final String NEW_FOLDER = "keyresources";

  @Override
  public void execute(
      TemporaryFileHandle staging, InstitutionInfo instInfo, ConverterParams params) {
    TemporaryFileHandle dynamicKeyResourceFolder = new SubTemporaryFile(staging, LEGACY_FOLDER);
    TemporaryFileHandle keyResourceFolder = new SubTemporaryFile(staging, NEW_FOLDER);

    xmlHelper
        .getXmlFileList(dynamicKeyResourceFolder)
        .forEach(
            xmlFilePath -> {
              final PropBagEx xml =
                  xmlHelper.readToPropBagEx(dynamicKeyResourceFolder, xmlFilePath);
              migrateDynamicKeyResourceXml(xml);
              // Write XML file to key resource folder.
              xmlHelper.writeFromPropBagEx(keyResourceFolder, xmlFilePath, xml);
            });
  }

  // Update attributes in dynamic key resource XML.
  private void migrateDynamicKeyResourceXml(PropBagEx dynamicKeyResourceXml) {
    dynamicKeyResourceXml.setNodeName("com.tle.beans.hierarchy.HierarchyTopicKeyResource");
    dynamicKeyResourceXml.renameNode("dynamicHierarchyId", "hierarchyCompoundUuid");
    dynamicKeyResourceXml.renameNode("uuid", "itemUuid");
    dynamicKeyResourceXml.renameNode("version", "itemVersion");
  }
}
