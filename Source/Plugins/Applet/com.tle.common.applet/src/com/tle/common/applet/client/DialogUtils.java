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

package com.tle.common.applet.client;

import com.tle.common.Pair;
import com.tle.common.i18n.CurrentLocale;
import java.awt.Component;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileSystemView;

public final class DialogUtils {
  private static final int MAX_SUGGEST_FILENAME_LENGTH = 100;

  private static File lastDirectory;

  private DialogUtils() {
    throw new Error();
  }

  public static final class DialogResult {
    private enum Result {
      OK,
      CANCEL
    }

    private Result result;
    private File file;

    private DialogResult(int result, File file, File directory) {
      if (result == JFileChooser.APPROVE_OPTION) {
        this.result = Result.OK;
        this.file = file;
        lastDirectory = directory;
      } else {
        this.result = Result.CANCEL;
        this.file = null;
      }
    }

    public boolean isOkayed() {
      return result == Result.OK;
    }

    public boolean isCancelled() {
      return result == Result.CANCEL;
    }

    public File getFile() {
      return file;
    }
  }

  public static String getSuggestedFileName(String itemName, String extension) {
    StringBuilder newName = new StringBuilder();

    int nameLength = itemName.length();
    for (int i = 0; i < nameLength && newName.length() < MAX_SUGGEST_FILENAME_LENGTH; i++) {
      final char c = itemName.charAt(i);
      if (Character.isLetterOrDigit(c)) {
        newName.append(c);
      } else if (Character.isWhitespace(c)) {
        newName.append(c);
      } else if (c == '_' || c == '-') {
        newName.append(c);
      }
    }

    return newName.append('.').append(extension).toString();
  }

  // OPEN
  public static DialogResult openDialog(Component parent, String title) {
    return doDialog(parent, true, title, null, true, null);
  }

  public static DialogResult openDialog(
      Component parent, String title, FileFilter filter, File defaultFile) {
    return doDialog(parent, true, title, filter, true, defaultFile);
  }

  public static DialogResult openDialogStrictFilter(
      Component parent, String title, FileFilter filter, File defaultFile) {
    return doDialog(parent, true, title, filter, false, defaultFile);
  }

  // SAVE
  public static DialogResult saveDialog(Component parent, String title) {
    return doDialog(parent, false, title, null, true, null);
  }

  public static DialogResult saveDialog(
      Component parent, String title, FileFilter filter, File defaultFile) {
    return doDialog(parent, false, title, filter, true, defaultFile);
  }

  public static DialogResult saveDialog(
      Component parent, String title, FileFilter filter, String defaultFileName) {
    return doDialog(parent, false, title, filter, true, getDefaultFile(defaultFileName));
  }

  public static DialogResult saveDialogStrictFilter(
      Component parent, String title, FileFilter filter, File defaultFile) {
    return doDialog(parent, false, title, filter, false, defaultFile);
  }

  /**
   * Does standard 'ok' clicked checking, and confirms overwrite before executing saver if
   * applicable
   *
   * @param parent
   * @param title
   * @param filter
   * @param defaultFileName
   * @param saver
   */
  public static void doSaveDialog(
      Component parent, String title, FileFilter filter, String defaultFileName, FileWorker saver) {
    final DialogResult result =
        doDialog(parent, false, title, filter, true, new File(defaultFileName));
    if (result.isOkayed()) {
      boolean writeFile = true;
      final File file = result.getFile();
      if (file.exists()) {
        final int result2 =
            JOptionPane.showConfirmDialog(
                parent,
                CurrentLocale.get("com.tle.common.gui.confirmoverwrite"), // $NON-NLS-1$
                CurrentLocale.get("com.tle.common.gui.overwrite"), // $NON-NLS-1$
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (result2 != JOptionPane.YES_OPTION) {
          writeFile = false;
        }
      }

      if (writeFile) {
        saver.setFile(file);
        saver.setComponent(parent);
        saver.start();
      }
    }
  }

  /**
   * Does standard 'ok' clicked checking, then executes loader if applicable
   *
   * @param parent
   * @param title
   * @param filter
   * @param loader
   */
  public static void doOpenDialog(
      Component parent, String title, FileFilter filter, FileWorker loader) {
    final DialogResult result = doDialog(parent, true, title, filter, true, null);
    if (result.isOkayed()) {
      loader.setFile(result.getFile());
      loader.setComponent(parent);
      loader.start();
    }
  }

  private static DialogResult doDialog(
      Component parent,
      boolean isOpenDialog,
      String title,
      FileFilter filter,
      boolean acceptAllFilter,
      File defaultFile) {
    JFileChooser chooser = new JFileChooser(getDirectory());
    if (title != null) {
      chooser.setDialogTitle(title);
    }
    if (filter != null) {
      chooser.setFileFilter(filter);
    }
    if (defaultFile != null) {
      chooser.setSelectedFile(defaultFile);
    }
    if (!acceptAllFilter) {
      chooser.setAcceptAllFileFilterUsed(false);
    }

    int result = 0;
    if (isOpenDialog) {
      result = chooser.showOpenDialog(parent);
    } else {
      result = chooser.showSaveDialog(parent);
    }
    return new DialogResult(result, chooser.getSelectedFile(), chooser.getCurrentDirectory());
  }

  private static File getDirectory() {
    if (lastDirectory == null) {
      lastDirectory = FileSystemView.getFileSystemView().getDefaultDirectory();
    }
    return lastDirectory;
  }

  private static File getDefaultFile(String defaultFileName) {
    Pair<String, String> unextended = removeExtension(defaultFileName);

    File defaultFile = new File(getDirectory(), defaultFileName);
    int i = 2;
    while (defaultFile.exists()) {
      defaultFile =
          new File(
              getDirectory(),
              unextended.getFirst()
                  + "("
                  + i
                  + ")."
                  + unextended.getSecond()); // $NON-NLS-1$ //$NON-NLS-2$
      i++;
    }
    return defaultFile;
  }

  private static Pair<String, String> removeExtension(String filename) {
    String unextended = filename;
    String extension = ""; // $NON-NLS-1$

    int dot = filename.lastIndexOf('.');
    if (dot > 0) {
      unextended = filename.substring(0, dot);
      if (filename.length() > dot) {
        extension = filename.substring(dot + 1);
      }
    }
    return new Pair<String, String>(unextended, extension);
  }
}
