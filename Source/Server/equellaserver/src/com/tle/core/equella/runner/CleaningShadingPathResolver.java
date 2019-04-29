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

package com.tle.core.equella.runner;

import java.io.File;
import org.java.plugin.standard.ShadingPathResolver;
import org.java.plugin.util.ExtendedProperties;

public class CleaningShadingPathResolver extends ShadingPathResolver {
  @Override
  @SuppressWarnings("nls")
  public synchronized void configure(ExtendedProperties config) throws Exception {
    File jpfShadow = new File(System.getProperty("java.io.tmpdir"), ".jpf-shadow");
    if (jpfShadow.exists()) {
      delete(jpfShadow);
    }
    super.configure(config);
  }

  @SuppressWarnings("nls")
  public static boolean delete(File file) {
    if (file.isDirectory()) {
      File[] fileList = file.listFiles();
      boolean succeeded = true;
      for (int i = 0; i < fileList.length; i++) {
        boolean thisSucceeded = delete(fileList[i]);
        succeeded &= thisSucceeded;
      }
      if (!succeeded) {
        return false;
      }
    }

    if (!file.exists()) {
      return true;
    }

    boolean deleted = file.delete();

    int attempt = 15;
    while (!deleted && attempt > 0) {
      try {
        Thread.sleep(200);
      } catch (InterruptedException e) {
        // nothing
      }
      deleted = file.delete();
      attempt--;
    }

    if (!deleted) {
      // For the stack trace. If a file fails to delete, it's probably
      // because we haven't closed a stream somewhere. We'd like to
      // know about it.
      throw new RuntimeException("Failed to delete:" + file);
    }
    return deleted;
  }
}
