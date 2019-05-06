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

package com.tle.core.hierarchy.migration;

import com.dytech.devlib.PropBagEx;
import com.tle.common.filesystem.handle.SubTemporaryFile;
import com.tle.common.filesystem.handle.TemporaryFileHandle;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.ConverterParams;
import com.tle.core.institution.convert.InstitutionInfo;
import com.tle.core.institution.convert.XmlMigrator;
import javax.inject.Singleton;

@Bind
@Singleton
@SuppressWarnings("nls")
public class RemoveCoursesMigrationXml extends XmlMigrator {
  @Override
  public void execute(
      TemporaryFileHandle staging, InstitutionInfo instInfo, ConverterParams params) {
    TemporaryFileHandle hierFolder = new SubTemporaryFile(staging, "hierarchy");
    for (String entry : xmlHelper.getXmlFileList(hierFolder)) {
      final PropBagEx xml = xmlHelper.readToPropBagEx(hierFolder, entry);

      // This works if there is one topic per file (new format)
      removeObsoleteXml(xml);

      // This works if there are many topics per file (old format)
      if (xml.nodeExists("com.tle.beans.hierarchy.HierarchyTopic")) {
        for (PropBagEx subxml : xml.iterator("com.tle.beans.hierarchy.HierarchyTopic")) {
          removeObsoleteXml(subxml);
        }
      }

      xmlHelper.writeFromPropBagEx(hierFolder, entry, xml);
    }
  }

  private void removeObsoleteXml(PropBagEx xml) {
    xml.deleteNode("courses");
    xml.deleteNode("parent/courses");
  }
}
