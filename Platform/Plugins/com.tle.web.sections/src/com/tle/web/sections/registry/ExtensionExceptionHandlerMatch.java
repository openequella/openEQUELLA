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

package com.tle.web.sections.registry;

import com.tle.core.plugins.PluginTracker;
import com.tle.web.sections.errors.SectionsExceptionHandler;
import org.java.plugin.registry.Extension;
import org.java.plugin.registry.Extension.Parameter;

public class ExtensionExceptionHandlerMatch implements ExceptionHandlerMatch {

  private final Extension extension;
  private final PluginTracker<SectionsExceptionHandler> pluginTracker;

  public ExtensionExceptionHandlerMatch(
      Extension extension, PluginTracker<SectionsExceptionHandler> pluginTracker) {
    this.extension = extension;
    this.pluginTracker = pluginTracker;
  }

  @Override
  public String getClassMatch() {
    Parameter param = extension.getParameter("exceptionClass");
    return param != null ? param.valueAsString() : null;
  }

  @Override
  public SectionsExceptionHandler getHandler() {
    return pluginTracker.getBeanByExtension(extension);
  }
}
