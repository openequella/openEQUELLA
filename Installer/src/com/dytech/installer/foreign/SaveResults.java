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

package com.dytech.installer.foreign;

import com.dytech.devlib.PropBagEx;
import com.dytech.installer.ForeignCommand;
import com.dytech.installer.InstallerException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class SaveResults extends ForeignCommand {
  private File file;

  public SaveResults(PropBagEx commandBag, PropBagEx resultBag) throws InstallerException {
    super(commandBag, resultBag);

    file = new File(getForeignValue("file"));
  }

  /*
   * (non-Javadoc)
   * @see com.dytech.installer.Command#execute()
   */
  @Override
  public void execute() throws InstallerException {
    file.getParentFile().mkdirs();
    if (!file.getParentFile().exists()) {
      throw new InstallerException("Could not create directory");
    }

    try (BufferedWriter out = new BufferedWriter(new FileWriter(file))) {
      out.write(resultBag.toString());
    } catch (IOException ex) {
      throw new InstallerException("Could not write file " + file.getAbsolutePath(), ex);
    }
  }

  /*
   * (non-Javadoc)
   * @see com.dytech.installer.Command#toString()
   */
  @Override
  public String toString() {
    return "Saving Installer Log";
  }
}
