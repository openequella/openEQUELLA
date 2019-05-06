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

package com.tle.core.institution.migration.v32;

import com.dytech.devlib.PropBagEx;
import com.tle.common.filesystem.handle.SubTemporaryFile;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.AbstractItemXmlMigrator;
import com.tle.core.institution.convert.ConverterParams;
import javax.inject.Singleton;

@Bind
@Singleton
@SuppressWarnings("nls")
public class UpdateHistoryEventMigrator extends AbstractItemXmlMigrator {
  @Override
  public boolean migrate(
      ConverterParams params, PropBagEx xml, SubTemporaryFile file, String filename)
      throws Exception {
    boolean changed = false;

    for (PropBagEx history : xml.iterateAll("history/com.tle.beans.item.HistoryEvent")) {
      history.deleteNode("item");
      PropBagEx d2 = history.getSubtree("date2");
      if (d2 != null) {
        d2.setNodeName("date");
      }
      changed = true;
    }

    return changed;
  }
}
