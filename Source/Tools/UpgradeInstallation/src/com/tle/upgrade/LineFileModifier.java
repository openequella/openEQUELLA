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

package com.tle.upgrade;

import com.google.common.collect.Lists;
import com.tle.common.Check;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

@SuppressWarnings("nls")
public abstract class LineFileModifier {
  private final File file;
  private final UpgradeResult result;
  private static String eol = System.getProperty("line.separator");

  public LineFileModifier(File propertiesFile, UpgradeResult result) {
    this.file = propertiesFile;
    this.result = result;
  }

  public void update() throws IOException {
    File parent = file.getParentFile();
    File bakFile = new File(parent, file.getName() + ".bak");
    new FileCopier(file, bakFile, true).rename();

    try (BufferedReader inFile =
            new BufferedReader(
                new InputStreamReader(new FileInputStream(bakFile), StandardCharsets.UTF_8));
        BufferedWriter outFile =
            new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
      String line;
      while ((line = inFile.readLine()) != null) {
        String newLine = processLine(line);
        if (newLine != null) {
          outFile.write(newLine);
          outFile.write(eol);
          if (result != null && !line.equals(newLine)) {
            result.addLogMessage("Changed '" + line + "' to '" + newLine + "'");
          }
        } else {
          if (result != null) {
            result.addLogMessage("Deleted line '" + line + "'");
          }
        }
      }

      // Add extra lines
      List<String> linesToAdd = addLines();
      if (!Check.isEmpty(linesToAdd)) {
        for (String addLine : linesToAdd) {
          outFile.write(addLine);
          outFile.write(eol);
          if (result != null) {
            result.addLogMessage("Added line '" + addLine + "'");
          }
        }
      }

    } catch (IOException t) {
      new FileCopier(bakFile, file, false).rename();

      throw new RuntimeException(t);
    }
    bakFile.delete();
  }

  protected abstract String processLine(String line);

  protected List<String> addLines() {
    // Override this
    return Lists.newArrayList();
  }
}
