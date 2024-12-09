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
import com.dytech.gui.workers.GlassSwingWorker;
import com.dytech.installer.Callback;
import com.dytech.installer.Wizard;
import java.awt.Component;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JOptionPane;

public class WebHostCallback implements Callback {
  public static final Pattern URL_PATT =
      Pattern.compile("^http://([\\.\\w\\-]+)(?::(\\d+))?(/[\\w/]*)?$");

  /*
   * (non-Javadoc)
   * @see com.dytech.installer.Callback#task(com.dytech.installer.Wizard)
   */
  @Override
  public void task(Wizard installer) {
    Component parent = installer.getFrame();

    PropBagEx output = installer.getOutputNow();
    String url = output.getNode("webserver/url").trim(); // $NON-NLS-1$
    Matcher m = URL_PATT.matcher(url);

    if (!m.matches()) {
      JOptionPane.showMessageDialog(
          parent,
          "You must specify the external URL of this"
              + " server.  Some examples of this are:\n - http://my.domain/\n - http://my."
              + "domain:8000\n - http://my.domain/some/context\n - http://my.domain:8000/"
              + "some/context\n",
          "Incorrect Server URL",
          JOptionPane.ERROR_MESSAGE);
      return;
    }

    System.out.println("Found " + m.groupCount() + " groups");
    for (int i = 1; i < m.groupCount() + 1; i++) {
      System.out.println("group " + i + " is '" + m.group(i) + "'");
    }

    String hostname = m.group(1);

    if (hostname.contains("_")) {
      JOptionPane.showMessageDialog(
          parent,
          "Hostnames cannot contain underscore"
              + " characters.  Please modify the hostname to remove this character"
              + " before continuing.",
          "Error",
          JOptionPane.ERROR_MESSAGE);
      return;
    }

    if (hostname.equalsIgnoreCase("localhost")
        || hostname.equals("127.0.0.1")) // $NON-NLS-1$ //$NON-NLS-2$
    {
      int result =
          JOptionPane.showConfirmDialog(
              parent,
              "You have entered 'localhost'"
                  + " or '127.0.0.1' as the external hostname.\nThese addresses are not valid"
                  + " external addresses, and are reserved for\nspecifying the local system."
                  + "  Using either of these values will restrict\nusers from using EQUELLA,"
                  + " except on this machine only.\n\nIt is strongly advised that you"
                  + " enter the hostname or IP address of this\nserver that is accessible by"
                  + " other computers on the network.  Are you\nsure that you wish to continue"
                  + " using 'localhost' or '127.0.0.1'?",
              "Warning!",
              JOptionPane.YES_NO_OPTION,
              JOptionPane.WARNING_MESSAGE);

      if (result != JOptionPane.YES_OPTION) {
        return;
      }
    }

    int port = 80;
    try {
      port = Integer.parseInt(m.group(2));
    } catch (NumberFormatException ex) {
      // port stays default.
    }

    if (port <= 0 || port > 65536) {
      JOptionPane.showMessageDialog(
          parent,
          "The server port must be an number between 1 and" + " 65536 inclusively.",
          "Error",
          JOptionPane.ERROR_MESSAGE);
      return;
    }

    checkCanConnect(installer, hostname, port);
    String prt = output.getNode("webserver/port").trim(); // $NON-NLS-1$
    System.out.println(prt);
  }

  private void checkCanConnect(final Wizard installer, final String hostname, final int port) {
    final Component parent = installer.getFrame();

    GlassSwingWorker<?> worker =
        new GlassSwingWorker<Object>() {
          /*
           * (non-Javadoc)
           * @see com.dytech.gui.workers.AdvancedSwingWorker#construct()
           */
          @Override
          public Object construct() throws Exception {
            ServerSocket socket = null;
            try {
              socket = new ServerSocket(port, 50, InetAddress.getByName(hostname));
            } finally {
              if (socket != null) {
                try {
                  socket.close();
                } catch (IOException ex) {
                  // Ignore this.
                }
              }
            }
            return null;
          }

          /*
           * (non-Javadoc)
           * @see com.dytech.gui.workers.AdvancedSwingWorker#finished()
           */
          @Override
          public void finished() {
            moveOn();
          }

          /*
           * (non-Javadoc)
           * @see com.dytech.gui.workers.AdvancedSwingWorker#exception()
           */
          @Override
          public void exception() {
            Exception ex = getException();
            if (ex instanceof UnknownHostException) {
              int result =
                  JOptionPane.showConfirmDialog(
                      parent,
                      "The hostname you supplied"
                          + " could not be found.\nAre you sure you want to continue?",
                      "Error",
                      JOptionPane.YES_NO_OPTION,
                      JOptionPane.ERROR_MESSAGE);

              if (result == JOptionPane.YES_OPTION) {
                moveOn();
              }
            } else {
              String message =
                  "Your computer could not bind to the given hostname and port."
                      + "  Please ensure that\n no other services are running on this port.\n\n";
              if (port < 1024) {
                message +=
                    "Please note that ports less than 1024 are restricted for" + " security.\n\n";
              }
              message += "Do you want to continue anyway?";

              int result =
                  JOptionPane.showConfirmDialog(
                      parent,
                      message,
                      "Warning",
                      JOptionPane.YES_NO_OPTION,
                      JOptionPane.WARNING_MESSAGE);

              if (result == JOptionPane.YES_OPTION) {
                moveOn();
              }
            }
          }

          private void moveOn() {
            installer.gotoPage(installer.getCurrentPageNumber() + 1);
          }
        };

    worker.setComponent(parent);
    worker.start();
  }
}
