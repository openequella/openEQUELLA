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

package com.tle.admin.controls.webdav;

import com.dytech.edge.wizard.beans.control.CustomControl;

public class WebDavControl extends CustomControl {
  private static final long serialVersionUID = -1129109182853406172L;
  private static final String AUTO_MARK_AS_RESOURCE = "autoMarkAsResource";

  public WebDavControl() {
    // This control used the be the File Manager control. But once the File Manager applet
    // was removed and the WebDAV support was enhanced (in 2023) it was renamed as the WebDAV
    // control. However, to ensure existing setups continue to work, the id of 'filemanager' has
    // been kept here and in the JPF files.
    setClassType("filemanager");
  }

  public boolean isAutoMarkAsResource() {
    Boolean b = (Boolean) getAttributes().get(AUTO_MARK_AS_RESOURCE);
    return b == null || b.booleanValue();
  }

  public void setAutoMarkAsResource(boolean b) {
    getAttributes().put(AUTO_MARK_AS_RESOURCE, b);
  }
}
