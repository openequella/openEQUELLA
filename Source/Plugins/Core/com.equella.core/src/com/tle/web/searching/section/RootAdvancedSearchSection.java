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

package com.tle.web.searching.section;

import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.RenderEventListener;
import com.tle.web.sections.render.HtmlRendererListener;
import com.tle.web.selection.SelectionSession;
import com.tle.web.selection.section.RootSelectionSection.Layout;
import com.tle.web.selection.section.SelectionSummarySection;

public class RootAdvancedSearchSection extends RootSearchSection {

  @Override
  public SectionResult renderHtml(RenderEventContext context) {
    SelectionSession selectionSession = selectionService.getCurrentSession(context);
    if (selectionSession != null) {
      // As we keep SelectionSummarySection in the tree for 'advanced/searching.do' regardless of
      // Selection Session's layout, we need to dynamically handle whether to show it or not.
      // The approach is to remove or add the RenderEventListener for SelectionSummarySection.
      SelectionSummarySection selectionSummarySection =
          context.lookupSection(SelectionSummarySection.class);
      String selectionSummarySectionId = selectionSummarySection.getSectionId();
      Layout selectionSessionLayout = selectionSession.getLayout();
      boolean selectionSummaryListenerExisting =
          context
                  .getTree()
                  .getListeners(selectionSummarySectionId, RenderEventListener.class)
                  .size()
              > 0;

      // Remove the listener in 'structured'.
      if (selectionSessionLayout == Layout.COURSE && selectionSummaryListenerExisting) {
        context.getTree().removeListeners(selectionSummarySectionId);
      }
      // Or add the listener back in 'selectOrAdd'.
      else if (selectionSessionLayout == Layout.NORMAL && !selectionSummaryListenerExisting) {
        context
            .getTree()
            .addListener(
                selectionSummarySectionId,
                RenderEventListener.class,
                new HtmlRendererListener(
                    selectionSummarySectionId, selectionSummarySection, getTree()));
      }
    }

    return super.renderHtml(context);
  }

  @Override
  public void registered(String id, SectionTree tree) {
    super.registered(id, tree);
  }

  @Override
  public boolean useNewSearch() {
    // Because we haven't built any new UI for Advanced search, return false
    // to keep using old UI.
    return false;
  }
}
