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
import com.tle.common.Check;
import java.awt.Component;
import java.io.IOException;
import java.net.ServerSocket;
import javax.swing.JOptionPane;

public class ManagerCallback implements Callback {
  @Override
  public void task(Wizard installer) {
    PropBagEx output = installer.getOutputNow();
    String port = output.getNode("service/port"); // $NON-NLS-1$

    int result = JOptionPane.YES_OPTION;
    Component parent = installer.getFrame();
    if (Check.isEmpty(port)) {
      result =
          JOptionPane.showConfirmDialog(
              parent,
              "You have not specified a port for openEQUELLA manager to run on. Should we use the"
                  + " default port (3000)?",
              "Warning", //$NON-NLS-1$ //$NON-NLS-2$
              JOptionPane.YES_NO_OPTION,
              JOptionPane.WARNING_MESSAGE);
      if (result == JOptionPane.YES_OPTION) {
        port = "3000";
      } else {
        return;
      }
    }

    try {
      ServerSocket socket = new ServerSocket(Integer.valueOf(port));
      socket.close();
    } catch (IOException e) {
      String message =
          "Your computer could not bind to the given port."
              + "  Please ensure that\n no other services are running on this port.\n\n";
      JOptionPane.showMessageDialog(parent, message);
      return;
    }

    output.setNode("service/port", port);
    installer.gotoPage(installer.getCurrentPageNumber() + 1);
  }
}
