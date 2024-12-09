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

package com.tle.web.sections.standard.js.modules;

import com.tle.core.i18n.CoreStrings;
import com.tle.core.javascript.JavascriptModule;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.render.PreRenderable;

@SuppressWarnings("nls")
public class StandardModule implements JavascriptModule {
  private static final long serialVersionUID = 1L;

  public static final ExternallyDefinedFunction SET_TIMEOUT =
      new ExternallyDefinedFunction("setTimeout");

  public static final ExternallyDefinedFunction POPUP_PERCENT =
      new ExternallyDefinedFunction("popup_percent_xy");

  @Override
  public String getDisplayName() {
    return CoreStrings.lookup().getString("js.modules.standard.name");
  }

  @Override
  public String getId() {
    return "standard";
  }

  @Override
  public PreRenderable getPreRenderer() {
    return null;
  }
}
