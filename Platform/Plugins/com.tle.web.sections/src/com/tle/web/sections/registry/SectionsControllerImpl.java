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

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.core.guice.Bind;
import com.tle.core.plugins.PluginService;
import com.tle.core.plugins.PluginTracker;
import com.tle.web.sections.SectionFilter;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.SectionsController;
import com.tle.web.sections.SectionsRuntimeException;
import com.tle.web.sections.errors.SectionsExceptionHandler;
import com.tle.web.sections.events.SectionEvent;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.java.plugin.registry.Extension;
import org.java.plugin.registry.Extension.Parameter;

/** @author jmaginnis */
@NonNullByDefault
@SuppressWarnings("nls")
@Bind(SectionsController.class)
@Singleton
public class SectionsControllerImpl extends AbstractSectionsController {
  @Inject private TreeRegistry treeRegistry;
  protected PluginTracker<SectionFilter> sectionFilters;
  protected PluginTracker<SectionsExceptionHandler> exceptionHandlers;

  @Inject
  public void setPluginService(PluginService pluginService) {
    sectionFilters =
        new PluginTracker<SectionFilter>(
            pluginService,
            "com.tle.web.sections",
            "sectionFilter",
            "id",
            new PluginTracker.ExtensionParamComparator("order"));
    sectionFilters.setBeanKey("class");

    exceptionHandlers =
        new PluginTracker<SectionsExceptionHandler>(
            pluginService,
            "com.tle.web.sections",
            "exceptionHandler",
            "class",
            new PluginTracker.ExtensionParamComparator("order"));
    exceptionHandlers.setBeanKey("class");
  }

  @Override
  protected SectionTree getTreeForPath(String path) {
    return treeRegistry.getTreeForPath(path);
  }

  @Override
  public List<SectionFilter> getSectionFilters() {
    return sectionFilters.getBeanList();
  }

  @Override
  public void handleException(
      SectionInfo info, Throwable exception, @Nullable SectionEvent<?> event) {
    if (exception instanceof SectionsRuntimeException) {
      if (exception.getCause() != null) {
        exception = exception.getCause();
      }
    }
    List<Extension> extensions = exceptionHandlers.getExtensions();
    for (Extension ext : extensions) {
      boolean handle;
      SectionsExceptionHandler handler = null;
      Parameter param = ext.getParameter("exceptionClass");
      if (param != null) {
        handle = exception.getClass().getName().equals(param.valueAsString());
      } else {
        handler = exceptionHandlers.getBeanByExtension(ext);
        handle = handler.canHandle(info, exception, event);
      }
      if (handle) {
        if (handler == null) {
          handler = exceptionHandlers.getBeanByExtension(ext);
        }
        // we don't expect to have a non-null handler, but to be sure ..
        if (handler != null) {
          handler.handle(exception, info, this, event);
          return;
        }
        // else continue until we either find a non-null handler, or
        // exit loop and call SectionUtils.throwRuntime
      }
    }
    SectionUtils.throwRuntime(exception);
  }
}
