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

package com.tle.core.scripting.guice;

import com.tle.core.config.guice.OptionalConfigModule;

public class SystemScriptModule extends OptionalConfigModule {

  public static final String ALLOWED_EXECUTABLES_KEY = "system.execute.allowedExecutables";

  @Override
  protected void configure() {
    // Empty by default: no executables are permitted until their absolute
    // path are added to the allow-list.
    bindProp(ALLOWED_EXECUTABLES_KEY, "");
  }
}
