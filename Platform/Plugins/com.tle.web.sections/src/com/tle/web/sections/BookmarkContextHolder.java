/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.web.sections;

import java.util.Collections;
import java.util.Set;

public class BookmarkContextHolder {
  private Set<String> contexts = Collections.emptySet();
  private Set<String> onlyForContext = Collections.emptySet();
  private Set<String> ignoreForContext = Collections.emptySet();

  public Set<String> getContexts() {
    return contexts;
  }

  public void setContexts(Set<String> contexts) {
    if (contexts.isEmpty()) {
      this.contexts = Collections.emptySet();
    } else {
      this.contexts = contexts;
    }
  }

  public Set<String> getOnlyForContext() {
    return onlyForContext;
  }

  public void setOnlyForContext(Set<String> onlyForContexts) {
    if (onlyForContexts.isEmpty()) {
      this.onlyForContext = Collections.emptySet();
    } else {
      this.onlyForContext = onlyForContexts;
    }
  }

  public Set<String> getIgnoreForContext() {
    return ignoreForContext;
  }

  public void setIgnoreForContext(Set<String> ignoreForContexts) {
    if (ignoreForContexts.isEmpty()) {
      this.ignoreForContext = Collections.emptySet();
    } else {
      this.ignoreForContext = ignoreForContexts;
    }
  }

  public boolean matches(BookmarkContextHolder contextHolder) {
    Set<String> allContexts = contextHolder.getContexts();
    if (!ignoreForContext.isEmpty() && !Collections.disjoint(ignoreForContext, allContexts)) {
      return false;
    }

    if (!onlyForContext.isEmpty() && Collections.disjoint(allContexts, onlyForContext)) {
      return false;
    }

    Set<String> ifc = contextHolder.getIgnoreForContext();
    if (!contexts.isEmpty() && !ifc.isEmpty() && !Collections.disjoint(contexts, ifc)) {
      return false;
    }

    Set<String> ofc = contextHolder.getOnlyForContext();
    if (!ofc.isEmpty() && Collections.disjoint(ofc, contexts)) {
      return false;
    }

    return true;
  }
}
