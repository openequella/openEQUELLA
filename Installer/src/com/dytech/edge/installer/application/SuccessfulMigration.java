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
import com.dytech.installer.ForeignCommand;
import com.dytech.installer.InstallerException;

public class SuccessfulMigration extends ForeignCommand {
  public SuccessfulMigration(PropBagEx commandBag, PropBagEx resultBag) throws InstallerException {
    super(commandBag, resultBag);
  }

  /*
   * (non-Javadoc)
   * @see com.dytech.installer.commands.Command#execute()
   */
  @Override
  public void execute() throws InstallerException {
    StringBuilder message = new StringBuilder();
    message.append("Data has been migrated successfully!\n\n");

    getProgress().popupMessage("Service Notes", message.toString(), false);
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "Final installation instructions";
  }
}
