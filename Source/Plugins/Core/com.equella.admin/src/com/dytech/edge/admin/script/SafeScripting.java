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

package com.dytech.edge.admin.script;

import com.tle.admin.Driver;
import com.tle.core.remoting.RemoteScriptingService;
import java.util.Collection;
import org.java.plugin.registry.Extension;

public class SafeScripting {

  private static boolean safeScripting =
      Driver.instance()
          .getClientService()
          .getService(RemoteScriptingService.class)
          .isSafeScripting();

  public static boolean isSafeScripting() {
    return safeScripting;
  }

  public static Collection<Extension> filterUnsafe(Collection<Extension> extensions) {
    if (!safeScripting) {
      return extensions;
    }
    return RemoteScriptingService.filterUnsafe(extensions);
  }
}
