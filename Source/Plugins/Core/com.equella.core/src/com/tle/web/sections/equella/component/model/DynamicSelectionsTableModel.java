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

package com.tle.web.sections.equella.component.model;

import com.google.common.collect.Lists;
import com.tle.annotation.NonNullByDefault;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.render.UnselectLinkRenderer;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.renderers.LinkRenderer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@NonNullByDefault
public abstract class DynamicSelectionsTableModel<T> implements SelectionsTableModel {
  protected abstract Collection<T> getSourceList(SectionInfo info);

  protected abstract void transform(
      SectionInfo info,
      SelectionsTableSelection selection,
      T thing,
      List<SectionRenderable> actions,
      int index);

  public final SelectionsTableSelection transform(SectionInfo info, T oldThing, int index) {
    List<SectionRenderable> actions = new ArrayList<SectionRenderable>();
    SelectionsTableSelection newThing = new SelectionsTableSelection();
    transform(info, newThing, oldThing, actions, index);
    newThing.setActions(actions);
    return newThing;
  }

  protected SectionRenderable makeAction(Label label, JSHandler action) {
    final HtmlLinkState actionLink = new HtmlLinkState(action);
    final LinkRenderer link = new LinkRenderer(actionLink);
    link.setLabel(label);
    return link;
  }

  protected SectionRenderable makeAction(Label label, JSHandler action, String id) {
    LinkRenderer link = (LinkRenderer) makeAction(label, action);
    link.setId(id);
    return link;
  }

  protected SectionRenderable makeRemoveAction(Label label, JSHandler action) {
    final HtmlLinkState actionLink = new HtmlLinkState(action);
    return new UnselectLinkRenderer(actionLink, label);
  }

  @Override
  public List<SelectionsTableSelection> getSelections(SectionInfo info) {
    final Collection<T> source = getSourceList(info);
    final List<SelectionsTableSelection> selections = Lists.newArrayList();
    if (source != null) {
      int index = 0;
      for (T thing : source) {
        selections.add(transform(info, thing, index));
        index++;
      }
    }
    return selections;
  }
}
