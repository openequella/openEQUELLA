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

package com.tle.web.sections.equella;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.core.services.user.UserSessionService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.generic.AbstractSectionFilter;
import javax.inject.Inject;

@NonNullByDefault
public abstract class AbstractModalSessionServiceImpl<S extends ModalSession>
    extends AbstractSectionFilter {
  protected SectionTree tree;

  @Inject protected UserSessionService sessionService;

  protected final void doSetupModalSession(
      SectionInfo info,
      S session,
      Class<? extends AbstractRootModalSessionSection<?>> sectionClass,
      Class<S> sessionClass) {
    final AbstractRootModalSessionSection<?> root = info.lookupSection(sectionClass);
    if (root == null) {
      throw new Error("No AbstractRootModalSessionSection found in trees");
    }
    String sessionId = null;
    if (session != null) {
      sessionId = sessionService.createUniqueKey();
      sessionService.setAttribute(sessionId, session);
    }
    info.setAttribute(sessionClass, session);
    root.setSessionId(info, sessionId);
  }

  @Override
  protected SectionTree getFilterTree() {
    synchronized (this) {
      if (tree == null) {
        tree = createFilterTree();
      }
      return tree;
    }
  }

  protected SectionTree createFilterTree() {
    throw new Error("Needs lookup-method"); // $NON-NLS-1$
  }

  @Nullable
  public abstract S getCurrentSession(SectionInfo info);
}
