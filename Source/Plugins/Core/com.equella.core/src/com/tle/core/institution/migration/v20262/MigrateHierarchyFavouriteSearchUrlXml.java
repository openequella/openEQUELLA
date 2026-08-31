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

package com.tle.core.institution.migration.v20262;

import com.dytech.devlib.PropBagEx;
import com.tle.common.filesystem.handle.SubTemporaryFile;
import com.tle.common.filesystem.handle.TemporaryFileHandle;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.ConverterParams;
import com.tle.core.institution.convert.InstitutionInfo;
import com.tle.core.institution.convert.XmlMigrator;
import javax.inject.Singleton;

/**
 * The XML counterpart of {@link MigrateHierarchyFavouriteSearchUrl}, so that favourite searches
 * exported before 2026.2 can still be imported.
 */
@Bind
@Singleton
public class MigrateHierarchyFavouriteSearchUrlXml extends XmlMigrator {
  private static final String FAVOURITE_SEARCH_FOLDER = "favourites/searches";
  private static final String URL_NODE = "url";

  @Override
  public void execute(
      TemporaryFileHandle staging, InstitutionInfo instInfo, ConverterParams params) {
    TemporaryFileHandle favouriteSearchFolder =
        new SubTemporaryFile(staging, FAVOURITE_SEARCH_FOLDER);

    xmlHelper
        .getXmlFileList(favouriteSearchFolder)
        .forEach(
            relativeXmlFilePath ->
                migrateFavouriteSearch(favouriteSearchFolder, relativeXmlFilePath));
  }

  // Rewrite the URL of a single exported favourite search, leaving the file alone unless the
  // compound UUID in it actually changes - most favourite searches are not for a hierarchy.
  private void migrateFavouriteSearch(
      TemporaryFileHandle favouriteSearchFolder, String relativeXmlFilePath) {
    final PropBagEx xml = xmlHelper.readToPropBagEx(favouriteSearchFolder, relativeXmlFilePath);
    final String url = xml.getNode(URL_NODE);
    final String migrated = MigrateHierarchyFavouriteSearchUrlHelper.toUrlSafeFormat(url);

    if (!migrated.equals(url)) {
      xml.setNode(URL_NODE, migrated);
      xmlHelper.writeFromPropBagEx(favouriteSearchFolder, relativeXmlFilePath, xml);
    }
  }
}
