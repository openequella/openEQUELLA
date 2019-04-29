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

package com.tle.web.sections.standard;

import com.google.common.collect.Maps;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.web.sections.BookmarkContextHolder;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SectionsRuntimeException;
import com.tle.web.sections.events.BookmarkEvent;
import com.tle.web.sections.events.DocumentParamsEvent;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import java.util.Map;
import java.util.Set;

@NonNullByDefault
public abstract class AbstractHtmlComponent<M> extends AbstractPrototypeSection<M> {
  @Nullable protected String preferredId;
  @Nullable protected String parameterId;
  protected boolean stateful = true;
  protected BookmarkContextHolder contextHolder = new BookmarkContextHolder();
  protected boolean finishedSetup;
  @Nullable private Map<Object, Object> componentAttributes;

  @Override
  public boolean isTreeIndexed() {
    return false;
  }

  public boolean isStateful() {
    return stateful;
  }

  public void setStateful(boolean stateful) {
    this.stateful = stateful;
  }

  @Override
  public String getDefaultPropertyName() {
    return preferredId;
  }

  @Override
  public void registered(String id, SectionTree tree) {
    super.registered(id, tree);
    if (parameterId == null) {
      parameterId = id;
    }
  }

  @Override
  public void treeFinished(String id, SectionTree tree) {
    finishedSetup = true;
  }

  @SuppressWarnings("nls")
  protected void ensureBuildingTree() {
    if (finishedSetup) {
      throw new SectionsRuntimeException(
          "Attempted to modify component settings after SectionTree has finished");
    }
  }

  protected boolean addToThisBookmark(SectionInfo info, BookmarkEvent event) {
    if ((!isStateful() && !event.isRendering()) || !event.isAllowedInThisContext(contextHolder)) {
      return false;
    }
    return true;
  }

  protected void addDocumentedParam(
      DocumentParamsEvent event, String param, String type, String... values) {
    event.addParam(
        contextHolder.getContexts().contains(BookmarkEvent.CONTEXT_SUPPORTED), param, type, values);
  }

  protected boolean hasBeenRendered(SectionInfo info) {
    return false;
  }

  public void setPreferredId(String preferredId) {
    this.preferredId = preferredId;
  }

  public String getParameterId() {
    return parameterId;
  }

  public void setParameterId(String parameterId) {
    this.parameterId = parameterId;
  }

  public void setOnlyForContext(Set<String> onlyForContexts) {
    contextHolder.setOnlyForContext(onlyForContexts);
  }

  public void setIgnoreForContext(Set<String> ignoreForContexts) {
    contextHolder.setIgnoreForContext(ignoreForContexts);
  }

  public void setContexts(Set<String> contexts) {
    contextHolder.setContexts(contexts);
  }

  public void setComponentAttribute(Object key, Object value) {
    ensureBuildingTree();

    if (componentAttributes == null) {
      componentAttributes = Maps.newHashMap();
    }
    componentAttributes.put(key, value);
  }

  @SuppressWarnings("unchecked")
  @Nullable
  public <T> T getComponentAttribute(Object key) {
    return componentAttributes == null ? null : (T) componentAttributes.get(key);
  }
}
