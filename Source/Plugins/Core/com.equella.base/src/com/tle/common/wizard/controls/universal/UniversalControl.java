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

package com.tle.common.wizard.controls.universal;

import com.dytech.edge.wizard.beans.control.CustomControl;

@SuppressWarnings("nls")
public class UniversalControl extends CustomControl {
  private static final long serialVersionUID = 1L;

  public static final String UNIVERSAL_CONTROL = "universal";

  public UniversalControl() {
    setClassType(UNIVERSAL_CONTROL);
  }

  public UniversalControl(CustomControl cloned) {
    if (cloned != null) {
      cloned.cloneTo(this);
    }
  }
}
