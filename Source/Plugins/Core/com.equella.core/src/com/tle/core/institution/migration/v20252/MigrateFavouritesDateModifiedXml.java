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

package com.tle.core.institution.migration.v20252;

import com.dytech.devlib.PropBagEx;
import com.tle.common.filesystem.handle.SubTemporaryFile;
import com.tle.common.filesystem.handle.TemporaryFileHandle;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.ConverterParams;
import com.tle.core.institution.convert.InstitutionInfo;
import com.tle.core.institution.convert.XmlMigrator;
import javax.inject.Singleton;

/**
 * The column "date_modified" for table "bookmark" and "favourite_search" has been renamed to
 * "added_at" to provide a clear meaning. To support the import of data exported before 2025.2, this
 * migration contains 2 steps:
 *
 * <p>1. Rename node "dateModified" to "addedAt".
 *
 * <p>2. Move favourite items folder from "myfavourites" to "favourites/items".
 */
@Bind
@Singleton
public class MigrateFavouritesDateModifiedXml extends XmlMigrator {
  private static final String OLD_FAVOURITE_ITEMS_FOLDER = "myfavourites";
  private static final String NEW_FAVOURITE_ITEMS_FOLDER = "favourites/items";
  private static final String FAVOURITE_SEARCH_FOLDER = "favourites/searches";

  private static final String DATE_MODIFIED_NODE = "dateModified";
  private static final String ADDED_AT_NODE = "addedAt";

  @Override
  public void execute(
      TemporaryFileHandle staging, InstitutionInfo instInfo, ConverterParams params) {
    TemporaryFileHandle newFavouriteItemFolder =
        new SubTemporaryFile(staging, NEW_FAVOURITE_ITEMS_FOLDER);
    TemporaryFileHandle favouriteSearchFolder =
        new SubTemporaryFile(staging, FAVOURITE_SEARCH_FOLDER);

    // Rename the folder.
    fileSystemService.move(staging, OLD_FAVOURITE_ITEMS_FOLDER, NEW_FAVOURITE_ITEMS_FOLDER);

    migrateFavourites(newFavouriteItemFolder);
    migrateFavourites(favouriteSearchFolder);
  }

  // Read XML files from the provided folder, rename the node "dateModified" to "addedAt", and write
  // it back to XML files.
  private void migrateFavourites(TemporaryFileHandle xmlFolder) {
    xmlHelper
        .getXmlFileList(xmlFolder)
        .forEach(
            relativeXmlFilePath -> {
              final PropBagEx xml = xmlHelper.readToPropBagEx(xmlFolder, relativeXmlFilePath);
              xml.renameNode(DATE_MODIFIED_NODE, ADDED_AT_NODE);
              xmlHelper.writeFromPropBagEx(xmlFolder, relativeXmlFilePath, xml);
            });
  }
}
