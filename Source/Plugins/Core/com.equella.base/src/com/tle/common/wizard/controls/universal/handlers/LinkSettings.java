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

package com.tle.common.wizard.controls.universal.handlers;

import com.dytech.edge.wizard.beans.control.CustomControl;
import com.tle.common.wizard.controls.universal.UniversalSettings;

public class LinkSettings extends UniversalSettings {
  private static final String DUPLICATION_CHECK = "LINK_DUPLICATION_CHECK";

  public LinkSettings(CustomControl customControl) {
    super(customControl);
  }

  public LinkSettings(UniversalSettings settings) {
    super(settings.getWrapped());
  }

  public boolean isDuplicationCheck() {
    return wrapped.getBooleanAttribute(DUPLICATION_CHECK, false);
  }

  public void setDuplicationCheck(boolean duplicationCheck) {
    wrapped.getAttributes().put(DUPLICATION_CHECK, duplicationCheck);
  }
}
