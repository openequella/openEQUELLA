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

/**
 * A utility class to help with line based file modifications. The file is backed up with a .bak
 * extension before any changes are made. If an error occurs during processing, the original file is
 * restored.
 *
 * <p>To use, extend this class and implement the processLine() or processLineMulti() methods. The
 * former is simpler, but only allows a single line to be returned. If you need to return multiple
 * lines, or remove a line, implement processLineMulti(). You can also override addLines() to add
 * extra lines at the end of the file.
 */
public abstract class LineFileModifier {
  private final File file;
  private final UpgradeResult result;
  private static final String eol = System.lineSeparator();

  public LineFileModifier(File targetFile, UpgradeResult result) {
    this.file = targetFile;
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
      processAllLines(inFile, outFile);
      addExtraLines(outFile);
    } catch (IOException t) {
      addLog("Error processing file - restoring original: " + t.getMessage());
      new FileCopier(bakFile, file, false).rename();

      throw new RuntimeException(t);
    }

    bakFile.delete();
  }

  private void processAllLines(BufferedReader inFile, BufferedWriter outFile) throws IOException {
    String line;
    while ((line = inFile.readLine()) != null) {
      List<String> newLines = processLineMulti(line);
      if (newLines.isEmpty()) {
        addLog("Deleted line '" + line + "'");
      } else {
        replaceLine(outFile, line, newLines);
      }
    }
  }

  private void addExtraLines(BufferedWriter outFile) throws IOException {
    List<String> linesToAdd = addLines();
    if (!Check.isEmpty(linesToAdd)) {
      for (String addLine : linesToAdd) {
        outFile.write(addLine);
        outFile.write(eol);
        addLog("Added line '" + addLine + "'");
      }
    }
  }

  private void replaceLine(BufferedWriter outFile, String line, List<String> newLines)
      throws IOException {
    boolean changeDetected = false;

    for (String newLine : newLines) {
      outFile.write(newLine);
      outFile.write(eol);
      if (!line.equals(newLine)) {
        changeDetected = true;
      }
    }

    if (changeDetected) {
      addLog("Replaced line '" + line + "' with:");
      for (String newLine : newLines) {
        addLog("  " + newLine);
      }
    }
  }

  private void addLog(String message) {
    if (result != null) {
      result.addLogMessage(message);
    }
  }

  /**
   * Process a line. Return the modified line, or null to delete the line.
   *
   * @param line The line to process
   * @return The modified line, or null to delete the line
   */
  protected abstract String processLine(String line);

  /**
   * A convenience method to process a line and return multiple lines. The default implementation
   * calls processLine() and returns a single item list or an empty list if null.
   *
   * @param line The line to process
   * @return A list of lines to write, or an empty list to delete the line
   */
  protected List<String> processLineMulti(String line) {
    String single = processLine(line);
    return single != null ? Lists.newArrayList(single) : Lists.newArrayList();
  }

  /**
   * Add extra lines at the end of the file. The default implementation returns an empty list.
   *
   * @return A list of lines to add at the end of the file
   */
  protected List<String> addLines() {
    // Override this
    return Lists.newArrayList();
  }
}
