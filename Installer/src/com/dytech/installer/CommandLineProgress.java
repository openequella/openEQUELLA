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

package com.dytech.installer;

import java.io.IOException;

public class CommandLineProgress implements Progress {
  /*
   * (non-Javadoc)
   * @see com.dytech.installer.Progress#addMessage(java.lang.String)
   */
  @Override
  public void addMessage(String msg) {
    System.out.println(msg);
  }

  /*
   * (non-Javadoc)
   * @see com.dytech.installer.Progress#getCurrentAmount()
   */
  @Override
  public int getCurrentAmount() {
    return 0;
  }

  /*
   * (non-Javadoc)
   * @see com.dytech.installer.Progress#getCurrentMaximum()
   */
  @Override
  public int getCurrentMaximum() {
    return 0;
  }

  /*
   * (non-Javadoc)
   * @see com.dytech.installer.Progress#getWholeAmount()
   */
  @Override
  public int getWholeAmount() {
    return 0;
  }

  /*
   * (non-Javadoc)
   * @see com.dytech.installer.Progress#getWholeMaximum()
   */
  @Override
  public int getWholeMaximum() {
    return 0;
  }

  /*
   * (non-Javadoc)
   * @see com.dytech.installer.Progress#setCurrentAmount(int)
   */
  @Override
  public void setCurrentAmount(int i) {
    // Ignore this
  }

  /*
   * (non-Javadoc)
   * @see com.dytech.installer.Progress#setCurrentMaximum(int)
   */
  @Override
  public void setCurrentMaximum(int maximum) {
    // Ignore this
  }

  /*
   * (non-Javadoc)
   * @see com.dytech.installer.Progress#setup(java.lang.String, int)
   */
  @Override
  public void setup(String title, int total) {
    // Ignore this
  }

  /*
   * (non-Javadoc)
   * @see com.dytech.installer.Progress#setWholeAmount(int)
   */
  @Override
  public void setWholeAmount(int i) {
    // Ignore this
  }

  /*
   * (non-Javadoc)
   * @see com.dytech.installer.Progress#popupMessage(java.lang.String,
   * java.lang.String, boolean)
   */
  @Override
  public void popupMessage(String title, String message, boolean error) {
    System.out.println("-------------------------------------------------");
    System.out.println(title.toUpperCase());
    System.out.println();
    System.out.println(message);
    System.out.println();
    System.out.println("Press ENTER to continue...");

    try {
      System.in.read();
    } catch (IOException e) {
      // Honestly don't care...
    }
  }
}
