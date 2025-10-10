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

import com.google.common.io.ByteStreams;
import com.tle.upgrade.FileCopier;
import com.tle.upgrade.UpgradeDepends;
import com.tle.upgrade.Upgrader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

@SuppressWarnings("nls")
public abstract class AbstractUpgrader implements Upgrader {
  protected static final String CONFIG_FOLDER = "learningedge-config";

  @Override
  public List<UpgradeDepends> getDepends() {
    return Collections.emptyList();
  }

  protected void obsoleteError() {
    throw new Error("This migration is obsolete and should not be being run");
  }

  public void copyResource(String resource, File dest) {
    copyResource(resource, dest, false);
  }

  public void copyResource(String resource, File dest, boolean executable) {
    String resname = new File(resource).getName();
    try (InputStream inp = getClass().getResourceAsStream(resource)) {
      if (dest.isDirectory()) {
        dest = new File(dest, resname);
      }
      dest.getParentFile().mkdirs();

      try (FileOutputStream out = new FileOutputStream(dest)) {
        ByteStreams.copy(inp, out);
        dest.setExecutable(executable, false);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  protected void rename(File from, File to) {
    rename(from, to, true, true);
  }

  protected void renameIfExists(File from, File to) {
    rename(from, to, false, true);
  }

  protected void rename(File from, File to, boolean mustExist, boolean overwrite) {
    try {
      new FileCopier(from, to, mustExist).rename();
    } catch (Exception e) {
      throw new RuntimeException("Failed to rename " + from + " to " + to);
    }
  }

  @Override
  public boolean isRunOnInstall() {
    return false;
  }

  protected Properties loadProperties(File file) throws IOException {
    try (FileInputStream finp = new FileInputStream(file)) {
      Properties props = new Properties();
      props.load(finp);
      return props;
    }
  }
}
