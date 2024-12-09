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
import com.tle.common.util.ExecUtils;
import java.awt.Component;
import java.io.File;
import javax.swing.JOptionPane;

public class JavaCallback implements Callback {
  /*
   * (non-Javadoc)
   * @see com.dytech.installer.Callback#task(com.dytech.installer.Wizard)
   */
  @Override
  public void task(Wizard installer) {
    PropBagEx output = installer.getOutputNow();
    String dir = output.getNode("java/jdk");
    // Use javac to determine if JDK is used.
    File javac =
        new File(
            dir
                + "/bin/javac"
                + (ExecUtils.PLATFORM_WIN64.equals(ExecUtils.determinePlatform()) ? ".exe" : ""));

    if (javac.exists()) {
      installer.gotoPage(installer.getCurrentPageNumber() + 1);
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
