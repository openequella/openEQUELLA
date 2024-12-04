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
import javax.swing.JOptionPane;

public class DatabaseHostCallback implements Callback {
  /** Class constructor. */
  public DatabaseHostCallback() {
    super();
  }

  /*
   * (non-Javadoc)
   * @see com.dytech.installer.Callback#task(com.dytech.installer.Wizard)
   */
  @Override
  public void task(Wizard installer) {
    PropBagEx output = installer.getOutputNow();

    String host = output.getNode("datasource/host").trim();

    int first = host.indexOf(':');
    int last = host.lastIndexOf(':');
    if (first >= 0 && first != last) {
      JOptionPane.showMessageDialog(
          installer.getFrame(),
          "Your database hostname is"
              + " malformed.  You must enter a valid hostname.\nIf you do not specify a"
              + " port number, then the default will be used.\nIf the database is running"
              + " on a port different to the default, then you\nmust specify it in the format"
              + " {hostname}:{port}",
          "Hostname Invalid",
          JOptionPane.ERROR_MESSAGE);
      return;
    }

    installer.gotoRelativePage(1);
  }
}
