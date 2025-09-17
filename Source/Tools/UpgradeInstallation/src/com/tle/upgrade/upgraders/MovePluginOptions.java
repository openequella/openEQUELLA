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

package com.tle.upgrade.upgraders;

import com.tle.upgrade.PropertyFileModifier;
import com.tle.upgrade.PropertyMover;
import com.tle.upgrade.UpgradeResult;
import java.io.File;
import java.util.regex.Pattern;

public class MovePluginOptions extends AbstractUpgrader {
  @Override
  public String getId() {
    return "MovePluginOptions"; //$NON-NLS-1$
  }

  @SuppressWarnings("nls")
  @Override
  public void upgrade(UpgradeResult result, File tleInstallDir) throws Exception {
    File configFolder = new File(tleInstallDir, CONFIG_FOLDER);
    File optionalConfig = new File(configFolder, PropertyFileModifier.OPTIONAL_CONFIG);
    File freeTextConfig =
        new File(configFolder, "plugins/com.tle.core.freetext/optional.properties");
    if (!freeTextConfig.exists()) {
      copyResource("freetext-optional.properties", freeTextConfig);
    }
    new PropertyMover(
            optionalConfig,
            freeTextConfig,
            Pattern.compile("(.*)((?:freetextIndex|textExtracter)\\.\\S*)\\s*=\\s*(.*)"),
            "")
        .move(result);

    new PropertyMover(
            optionalConfig,
            new File(configFolder, "plugins/com.tle.web.search/optional.properties"),
            Pattern.compile("(.*)rssHelper(\\.\\S*)\\s*=\\s*(.*)"),
            "com.tle.web.search.searcher.RSSSearchHelper")
        .move(result);

    new PropertyMover(
            optionalConfig, null, Pattern.compile("(.*)loginService(\\.\\S*)\\s*=\\s*(.*)"), "")
        .move(result);
  }

  @Override
  public boolean canBeRemoved() {
    return true;
  }
}
