/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dytech.installer.commands;

import java.io.File;
import java.io.IOException;

import com.dytech.common.io.ZipUtils;
import com.dytech.installer.InstallerException;

public class CZipExtract extends Command {
  protected String source;
  protected String destination;
  protected String pattern;

  public CZipExtract(String source, String destination) {
    this(source, destination, ".*");
  }

  public CZipExtract(String source, String destination, String pattern) {
    this.source = source;
    this.destination = destination;
    this.pattern = pattern;
  }

  @Override
  public void execute() throws InstallerException {
    propogateTaskStarted(1);

    if (source.endsWith("*")) {
      source = source.substring(0, source.length() - 1);
      File f = new File(source);
      if (!f.exists()) {
        throw new InstallerException(f.toString() + " does not exist");
      }

      if (!f.isDirectory()) {
        throw new InstallerException(f.toString() + " is not a directory");
      }

      File[] children = f.listFiles();
      if (children.length == 0) {
        throw new InstallerException(f.toString() + " does not have any files");
      }

      source = children[0].toString();
    }

    try {
      ZipUtils.extract(new File(source), ZipUtils.createZipFilter(pattern), new File(destination));
    } catch (IOException ex) {
      final String message =
          ""
              + "Fatal Error Extracting File From Zip:\n"
              + "Zip Source = "
              + source
              + '\n'
              + "Destination = "
              + destination
              + '\n'
              + "Pattern = "
              + pattern;
      throw new InstallerException(message, ex);
    }

    propogateSubtaskCompleted();
    propogateTaskCompleted();
  }

  @Override
  public String toString() {
    return new String("Extracting from " + source + " to " + destination);
  }
}
