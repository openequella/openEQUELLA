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

package com.dytech.installer.commands;

import com.dytech.installer.InstallerException;
import java.io.File;

public class CMkExec extends Command {

  private final String file;

  public CMkExec(String file) {
    this.file = file;
  }

  @Override
  public void execute() throws InstallerException {

    new File(file).setExecutable(true);
  }

  @Override
  public String toString() {
    return "Making " + file + " executable";
  }
}
