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

package com.dytech.edge.installer.application;

import com.dytech.devlib.PropBagEx;
import com.dytech.installer.Callback;
import com.dytech.installer.Wizard;
import java.io.File;
import java.util.Arrays;
import java.util.Optional;
import javax.swing.JOptionPane;

/**
 * Validates the user-specified ImageMagick installation directory during the installation wizard
 * workflow.
 */
public class ImageMagickCallback implements Callback {

  /**
   * The ImageMagick configuration key used to retrieve the installation path from wizard output.
   */
  private static final String IMAGEMAGICK_PATH_KEY = "imagemagick/path";

  /** The name of the primary ImageMagick executable to locate. */
  private static final String MAGICK_EXE_NAME = "magick";

  /**
   * Executable file extensions to check, in order of preference. An empty string handles Unix/macOS
   * binaries with no extension; {@code .exe} handles Windows binaries.
   */
  private static final String[] EXE_EXTENSIONS = {"", ".exe"};

  /** Dialog title used for all ImageMagick directory validation error messages. */
  private static final String ERROR_TITLE = "Incorrect ImageMagick Directory";

  /**
   * Validates the ImageMagick installation directory configured in the wizard and, if valid,
   * advances the wizard to the next page.
   *
   * @param installer the active {@link Wizard} instance providing configuration and navigation
   */
  @Override
  public void task(Wizard installer) {
    PropBagEx output = installer.getOutputNow();
    File dir = new File(output.getNode(IMAGEMAGICK_PATH_KEY));

    if (!isValidDirectory(dir)) {
      showError(
          installer,
          "You have not specified a valid directory.\n"
              + "Please select the correct path, and try again.");
      return;
    }

    Optional<File> magickExe = findExe(dir);
    if (magickExe.isEmpty()) {
      showError(
          installer,
          "The directory you have specified does not contain the ImageMagick program '"
              + MAGICK_EXE_NAME
              + "'.\nPlease select the correct path, and try again.");
      return;
    }

    installer.gotoPage(installer.getCurrentPageNumber() + 1);
  }

  /**
   * Determines whether the given {@link File} represents an existing directory.
   *
   * @param dir the file to check
   * @return {@code true} if {@code dir} exists and is a directory; {@code false} otherwise
   */
  private boolean isValidDirectory(File dir) {
    return dir.exists() && dir.isDirectory();
  }

  /**
   * Searches the given directory for an executable file matching the provided name, checking each
   * known {@link #EXE_EXTENSIONS extension} in order.
   *
   * @param directory the directory in which to search
   * @return an {@link Optional} containing the first matching executable {@link File}, or {@link
   *     Optional#empty()} if none is found
   */
  private Optional<File> findExe(File directory) {
    return Arrays.stream(EXE_EXTENSIONS)
        .map(ext -> new File(directory, MAGICK_EXE_NAME + ext))
        .filter(File::canExecute)
        .findFirst();
  }

  /**
   * Displays a modal error dialog attached to the wizard's frame.
   *
   * @param installer the active {@link Wizard}
   * @param message the error message to display to the user
   */
  private void showError(Wizard installer, String message) {
    JOptionPane.showMessageDialog(
        installer.getFrame(), message, ERROR_TITLE, JOptionPane.ERROR_MESSAGE);
  }
}
