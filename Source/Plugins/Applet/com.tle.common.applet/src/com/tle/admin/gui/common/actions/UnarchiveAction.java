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

package com.tle.admin.gui.common.actions;

import com.tle.common.i18n.CurrentLocale;
import javax.swing.Action;

public abstract class UnarchiveAction extends TLEAction {
  private static final long serialVersionUID = 1L;

  public UnarchiveAction() {
    setIcon("/icons/unarchive.gif"); // $NON-NLS-1$
    putValue(
        Action.NAME,
        CurrentLocale.get("com.tle.admin.gui.common.actions.unarchiveaction.name")); // $NON-NLS-1$
    putValue(
        Action.SHORT_DESCRIPTION,
        CurrentLocale.get("com.tle.admin.gui.common.actions.unarchiveaction.desc")); // $NON-NLS-1$
  }
}
