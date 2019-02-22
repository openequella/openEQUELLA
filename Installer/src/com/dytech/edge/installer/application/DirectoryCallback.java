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

package com.dytech.edge.installer.application;

import com.dytech.devlib.PropBagEx;
import com.dytech.installer.Callback;
import com.dytech.installer.Wizard;
import java.awt.Component;
import java.io.File;
import java.io.IOException;
import javax.swing.JOptionPane;

/*
 * @author Nicholas Read
 */
public class DirectoryCallback implements Callback {
  /*
   * (non-Javadoc)
   * @see com.dytech.installer.Callback#task(com.dytech.installer.Wizard)
   */
  @Override
  public void task(Wizard installer) {
    PropBagEx output = installer.getOutputNow();

    final String installPath = output.getNode("install.path");

    if ("".equals(installPath)) {
      Component parent = installer.getFrame();
      JOptionPane.showMessageDialog(
          parent,
          "Please specify a valid directory.",
          "Invalid Directory",
          JOptionPane.ERROR_MESSAGE);
      return;
    }

    if (installPath.trim().indexOf(" ") != -1) {
      Component parent = installer.getFrame();
      JOptionPane.showMessageDialog(
          parent,
          "The install location must not contain whitespace.",
          "Invalid Directory",
          JOptionPane.ERROR_MESSAGE);
      return;
    }

    final File dir = new File(installPath);

    if (dir.exists()) {
      String[] files = dir.list();
      if (files != null && files.length > 0) {
        System.out.print("Contains files: ");
        for (String file : files) {
          System.out.print(file);
          System.out.print(", ");
        }
        System.out.println();

        if (JOptionPane.showConfirmDialog(
                installer.getFrame(),
                "The selected directory contains files that will be deleted before\n" //$NON-NLS-1$
                    + "installation.  Do you still want to use this directory?",
                "Warning", //$NON-NLS-1$ //$NON-NLS-2$
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE)
            != JOptionPane.YES_OPTION) {
          return;
        }
      }
    }

    File tmp = null;
    try {
      dir.mkdirs();
      tmp = File.createTempFile("tle", "tmp", dir);

      int page = installer.getCurrentPageNumber();
      installer.gotoPage(page + 1);
    } catch (IOException ex) {
      Component parent = installer.getFrame();
      JOptionPane.showMessageDialog(
          parent,
          "The installer cannot write files to the"
              + " specified directory.\nPlease check that you have permission to create files"
              + " in the\ndirectory, or specify a different path.",
          "Could Not Write To Directory",
          JOptionPane.ERROR_MESSAGE);
    } finally {
      if (tmp != null) {
        tmp.delete();
      }
    }
  }
}
