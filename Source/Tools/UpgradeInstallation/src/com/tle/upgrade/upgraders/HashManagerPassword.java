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

import com.tle.common.Check;
import com.tle.common.hash.Hash;
import com.tle.upgrade.UpgradeResult;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map.Entry;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("nls")
public class HashManagerPassword extends AbstractUpgrader {
  private static final Logger LOGGER = LoggerFactory.getLogger(HashManagerPassword.class);

  @Override
  public String getId() {
    return "HashManagerPassword";
  }

  @Override
  public boolean canBeRemoved() {
    return true;
  }

  @Override
  public void upgrade(UpgradeResult result, File tleInstallDir) throws Exception {
    final Properties userPassMap = new Properties();
    final File users = new File(tleInstallDir, "manager/users.properties");

    int count = 0;
    try (InputStream in = new BufferedInputStream(new FileInputStream(users))) {
      userPassMap.load(in);

      for (Entry<Object, Object> entry : userPassMap.entrySet()) {
        final String val = (String) entry.getValue();
        if (!Check.isEmpty(val)) {
          if (!Hash.isHashed(val)) {
            count++;
            entry.setValue(Hash.hashPassword(val));
          }
        }
      }
    }

    try (OutputStream out = new FileOutputStream(users)) {
      userPassMap.store(out, null);
    }

    LOGGER.info("Hashed " + count + " manager user passwords");
  }
}
