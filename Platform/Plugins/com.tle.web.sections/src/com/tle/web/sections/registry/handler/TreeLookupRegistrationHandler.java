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

package com.tle.web.sections.registry.handler;

import com.tle.core.guice.Bind;
import com.tle.web.sections.Section;
import com.tle.web.sections.SectionTree;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Singleton;

@Bind
@Singleton
public class TreeLookupRegistrationHandler
    extends CachedScannerHandler<AnnotatedTreeLookupScanner> {

  private static final String KEY_TREELOOKUPS = "$TREE-LOOKUPS$"; // $NON-NLS-1$

  @Override
  protected AnnotatedTreeLookupScanner newEntry(Class<?> clazz) {
    return new AnnotatedTreeLookupScanner(clazz, this);
  }

  @Override
  public void registered(String id, SectionTree tree, Section section) {
    List<String> ids = tree.getAttribute(KEY_TREELOOKUPS);
    if (ids == null) {
      ids = new ArrayList<String>();
      tree.setAttribute(KEY_TREELOOKUPS, ids);
    }
    AnnotatedTreeLookupScanner scanner = getForClass(section.getClass());
    if (scanner.hasLookups()) {
      ids.add(id);
    }
  }

  @Override
  public void treeFinished(SectionTree tree) {
    List<String> ids = tree.getAttribute(KEY_TREELOOKUPS);
    // can happen in the case of an empty tree (see
    // RenderSummarySectionViewItem)
    if (ids != null) {
      for (String id : ids) {
        Section section = tree.getSectionForId(id);
        AnnotatedTreeLookupScanner scanner = getForClass(section.getClass());
        scanner.doLookup(tree, section);
      }
    }
  }
}
