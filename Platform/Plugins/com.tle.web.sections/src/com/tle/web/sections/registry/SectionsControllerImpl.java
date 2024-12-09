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
import com.tle.core.guice.Bind;
import com.tle.core.plugins.PluginService;
import com.tle.core.plugins.PluginTracker;
import com.tle.web.sections.SectionFilter;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SectionsController;
import com.tle.web.sections.errors.SectionsExceptionHandler;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Singleton;

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

    exceptionHandlers = createExceptionTracker(pluginService);
  }

  public static PluginTracker<SectionsExceptionHandler> createExceptionTracker(
      PluginService pluginService) {
    PluginTracker<SectionsExceptionHandler> tracker =
        new PluginTracker<SectionsExceptionHandler>(
            pluginService,
            "com.tle.web.sections",
            "exceptionHandler",
            "class",
            new PluginTracker.ExtensionParamComparator("order"));
    tracker.setBeanKey("class");
    return tracker;
  }

  @Override
  protected SectionTree getTreeForPath(String path) {
    return treeRegistry.getTreeForPath(path);
  }

  @Override
  public List<SectionFilter> getSectionFilters() {
    return sectionFilters.getBeanList();
  }

  protected Collection<ExceptionHandlerMatch> getExceptionHandlers() {
    return exceptionHandlers.getExtensions().stream()
        .map(e -> new ExtensionExceptionHandlerMatch(e, exceptionHandlers))
        .collect(Collectors.toList());
  }
}
