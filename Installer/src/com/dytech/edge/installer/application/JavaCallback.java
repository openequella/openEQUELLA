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

import java.awt.Component;
import java.io.File;

import javax.swing.JOptionPane;

import com.dytech.devlib.PropBagEx;
import com.dytech.installer.Callback;
import com.dytech.installer.Wizard;

/** @author Nicholas Read */
public class JavaCallback implements Callback {
  /*
   * (non-Javadoc)
   * @see com.dytech.installer.Callback#task(com.dytech.installer.Wizard)
   */
  @Override
  public void task(Wizard installer) {
    PropBagEx output = installer.getOutputNow();
    String dir = output.getNode("java/jdk"); // $NON-NLS-1$
    File tools = new File(dir + "/lib/tools.jar"); // $NON-NLS-1$

    if (tools.exists()) {
      int result = JOptionPane.YES_OPTION;
      if (dir.indexOf("1.8") == -1) // $NON-NLS-1$
      {
        Component parent = installer.getFrame();
        result =
            JOptionPane.showConfirmDialog(
                parent,
                "Does the directory refer to a Java 8 installation?",
                "Warning", //$NON-NLS-1$ //$NON-NLS-2$
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
      }

      if (result == JOptionPane.YES_OPTION) {
        installer.gotoPage(installer.getCurrentPageNumber() + 1);
      }
    } else {
      Component parent = installer.getFrame();
      JOptionPane.showMessageDialog(
          parent,
          "You have not specified a valid Java directory."
              + "\nPlease select the correct path, and try again.",
          "Incorrect Java Directory",
          JOptionPane.ERROR_MESSAGE);
    }
  }
}
