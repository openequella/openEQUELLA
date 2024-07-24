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
import com.tle.beans.item.ItemId;
import com.tle.common.filesystem.FileHandleUtils;
import com.tle.common.filesystem.handle.SubTemporaryFile;
import com.tle.common.filesystem.handle.TemporaryFileHandle;
import com.tle.common.util.Dates;
import com.tle.core.guice.Bind;
import com.tle.core.hierarchy.convert.KeyResourceConverter;
import com.tle.core.institution.convert.ConverterParams;
import com.tle.core.institution.convert.InstitutionInfo;
import com.tle.core.institution.convert.XmlMigrator;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import javax.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The table for hierarchy key resource has been refactored (such as delete foreign key constrains
 * from hierarchy) to support store both normal and dynamic key resources. To support the import of
 * data exported before 2024.2, this migration is created to update the XML file contents for
 * hierarchy and key resources.
 *
 * <p>The migration process is as follows:
 *
 * <p>1. Traverse the hierarchy folder to find out all normal key resources in the hierarchy XML.
 *
 * <p>2. Based on the hierarchy XML format it will extract all key resources.
 *
 * <p>There are 2 formats: one is the oldest format, stores all hierarchy in a single `hierachy.xml`
 * file under <list></list> tag.
 *
 * <p>The other format stores each hierarchy in a separate file without <list></list> tag.
 *
 * <p>3. For each normal key resource, based on the item ID in XML, it will try to find out the item
 * XML file.
 *
 * <p>4. and then extract the item UUID and VERSION from item XML file. If the item is not found, it
 * will ignore this key resource and log the error.
 *
 * <p>5. Generate a random database ID for new key resource structures. And then based on the new
 * ID, it will generate a number for its parent folder name.
 *
 * <p>6. Once it gets all the information, it creates a new XML file under the `keyresources` folder
 * with the format [folder_name_based on_new_id]/[new_random_id].xml.
 *
 * <p>For example:
 *
 * <p>from:
 *
 * <pre>{@code
 * <keyResources>
 *   <com.tle.beans.item.Item>
 *     <id>117276</id>
 *   </com.tle.beans.item.Item>
 * </keyResources>
 * }</pre>
 *
 * To:
 *
 * <pre>{@code
 * <com.tle.beans.hierarchy.HierarchyTopicKeyResource>
 *   <id>newRandomId</id>
 *   <hierarchyCompoundUuid>886aa61d-f8df-4e82-8984-c487849f80ff:A+James</hierarchyCompoundUuid>
 *   <itemUuid>e35390cf-7c45-4f71-bb94-e6ccc1f09394</itemUuid>
 *   <itemVersion>1</itemVersion>
 *   <dateCreated class="sql-timestamp">2023-11-17 09:09:44.485</dateCreated>
 * </com.tle.beans.hierarchy.HierarchyTopicKeyResource>
 * }</pre>
 */
@Bind
@Singleton
public class MigrateKeyResourceFromHierarchyXml extends XmlMigrator {
  private static final Logger LOGGER =
      LoggerFactory.getLogger(MigrateKeyResourceFromHierarchyXml.class);
  private static final String HIERARCHY_NODE = "com.tle.beans.hierarchy.HierarchyTopic";
  private static final String NEW_KEY_RESOURCE_NODE =
      "com.tle.beans.hierarchy.HierarchyTopicKeyResource";
  private static final String OLD_KEY_RESOURCE_PATH = "keyResources/com.tle.beans.item.Item";
  private static final String SUB_HIERARCHY_NODE =
      "subTopics/com.tle.beans.hierarchy.HierarchyTopic";

  @Override
  public void execute(
      TemporaryFileHandle staging, InstitutionInfo instInfo, ConverterParams params) {
    TemporaryFileHandle hierFolder = new SubTemporaryFile(staging, "hierarchy");
    for (String entry : xmlHelper.getXmlFileList(hierFolder)) {
      final PropBagEx xml = xmlHelper.readToPropBagEx(hierFolder, entry);

      // Handle two formats of hierarchy XML: list and single hierarchy XML.
      if ("list".equals(xml.getNodeName())) {
        xml.iterator(HIERARCHY_NODE)
            .forEach(hierarchyXml -> migrateHierarchyKeyResources(hierarchyXml, staging));
      } else {
        migrateHierarchyKeyResources(xml, staging);
      }
      // Overwrite the hierarchy XML file without the keyResources node.
      xmlHelper.writeFromPropBagEx(hierFolder, entry, xml);
    }
  }

  // Get item version and uuid from item xml based on the given item DB ID.
  private ItemId getItemVersionAndUuidFromItemXml(int itemDbId, TemporaryFileHandle staging) {
    // Item folder
    TemporaryFileHandle itemsFolder = new SubTemporaryFile(staging, "items");
    // Item path
    String itemXmlFilePath =
        Paths.get(FileHandleUtils.getBucketFolder(itemDbId), itemDbId + ".xml").toString();

    final PropBagEx itemXml = xmlHelper.readToPropBagEx(itemsFolder, itemXmlFilePath);
    return new ItemId(itemXml.getNode("uuid"), itemXml.getIntNode("version"));
  }

  // Create new keyResource XML based on the keyResource attribute in hierarchy XML.
  private void createNewXmlForHierarchyKeyResource(
      String hierarchyUuid, PropBagEx keyResourceItemXml, TemporaryFileHandle staging) {
    // Get key resource item ID.
    int keyResourceItemDbId = keyResourceItemXml.getIntNode("id");
    // Get item version and uuid from item XML.
    ItemId itemVersionAndUuid = getItemVersionAndUuidFromItemXml(keyResourceItemDbId, staging);

    PropBagEx newKeyResourceXml = new PropBagEx();
    // No node for ID since it will be autogenerated in DB.
    newKeyResourceXml.setNodeName(NEW_KEY_RESOURCE_NODE);
    newKeyResourceXml.createNode("hierarchyCompoundUuid", hierarchyUuid);
    newKeyResourceXml.createNode("itemVersion", Integer.toString(itemVersionAndUuid.getVersion()));
    newKeyResourceXml.createNode("itemUuid", itemVersionAndUuid.getUuid());
    newKeyResourceXml.createNode(
        "dateCreated", new SimpleDateFormat(Dates.ISO_DATE_TIME.toString()).format(new Date()));
    newKeyResourceXml.setAttribute("dateCreated", "class", "sql-timestamp");

    // Generate a unique file name for key resource.
    String xmlFilePath = UUID.randomUUID() + ".xml";
    TemporaryFileHandle keyResourceFolder =
        new SubTemporaryFile(staging, KeyResourceConverter.KEY_RESOURCES_IMPORT_EXPORT_FOLDER);
    // Write XML file to key resource folder.
    xmlHelper.writeFromPropBagEx(keyResourceFolder, xmlFilePath, newKeyResourceXml);
  }

  // Remove old keyResource node from hierarchy XML, and create new keyResource XML.
  // And it's a recursive function in order to handle sub topics.
  private void migrateHierarchyKeyResources(PropBagEx xml, TemporaryFileHandle staging) {
    String hierarchyUuid = xml.getNode("uuid");
    // Traverse all key resources in hierarchy XML and create new key resource XML for each key
    // resource.
    xml.iterator(OLD_KEY_RESOURCE_PATH)
        .forEach(
            keyResourceItemXml -> {
              try {
                // convert old keyResource node to new keyResource XML
                createNewXmlForHierarchyKeyResource(hierarchyUuid, keyResourceItemXml, staging);
              } catch (RuntimeException e) {
                LOGGER.error(
                    "Can't migrate key resource for hierarchy {} : {}",
                    hierarchyUuid,
                    e.getMessage());
              }
            });

    // remove old keyResource from hierarchy XML
    xml.deleteNode("keyResources");

    // handle sub topics
    if (xml.nodeExists("subTopics")) {
      for (PropBagEx subXml : xml.iterator(SUB_HIERARCHY_NODE)) {
        migrateHierarchyKeyResources(subXml, staging);
      }
    }
  }
}
