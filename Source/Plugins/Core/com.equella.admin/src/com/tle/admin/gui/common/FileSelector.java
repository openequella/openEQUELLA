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

package com.tle.admin.gui.common;

import com.dytech.gui.file.JFileSelector;
import com.tle.common.applet.client.DialogUtils;
import com.tle.common.applet.client.DialogUtils.DialogResult;
import com.tle.common.i18n.CurrentLocale;
import javax.swing.filechooser.FileFilter;

public class FileSelector extends JFileSelector {
  /**
   * This is why things should NEVER be made private unless you have a very good reason to. This
   * pisses me off greatly.
   */
  protected FileFilter protectedFileFilter;

  protected final String dialogTitle;

  public FileSelector(String dialogTitle) {
    this(CurrentLocale.get("com.tle.admin.gui.common.browse"), dialogTitle); // $NON-NLS-1$
  }

  public FileSelector(String browseButtonText, String dialogTitle) {
    super();
    this.dialogTitle = dialogTitle;
    button.setText(browseButtonText);
  }

  @Override
  protected void buttonSelected() {
    DialogResult result =
        DialogUtils.openDialog(getParent(), dialogTitle, protectedFileFilter, null);
    if (result.isOkayed()) {
      setSelectedFile(result.getFile());
    }
  }

  @Override
  public void setFileFilter(FileFilter filter) {
    protectedFileFilter = filter;
  }
}
