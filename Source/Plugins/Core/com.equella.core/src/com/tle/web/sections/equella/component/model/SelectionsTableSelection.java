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
import com.tle.common.Check;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.AbstractTable;
import com.tle.web.sections.standard.model.TableState.TableCell;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The class formerly known as CurrentlySelectedThing
 *
 * @author aholland
 */
public class SelectionsTableSelection // (virtually a TableRow)
 {
  private List<SectionRenderable> actions;
  // Excludes the first cell and the actions cell
  private final List<TableCell> cells;

  // First cell info
  private Label name;
  private SectionRenderable icon;
  private SectionRenderable view;
  private Set<String> styleClasses;

  public SelectionsTableSelection() {
    cells = Lists.newArrayList();
  }

  public void setIcon(SectionRenderable icon) {
    this.icon = icon;
  }

  public void setName(Label name) {
    this.name = name;
  }

  public void setViewAction(SectionRenderable view) {
    this.view = view;
  }

  public SectionRenderable getIcon() {
    return icon;
  }

  public Label getName() {
    return name;
  }

  public SectionRenderable getViewAction() {
    return view;
  }

  public List<SectionRenderable> getActions() {
    return actions;
  }

  public void setActions(List<SectionRenderable> actions) {
    this.actions = actions;
  }

  public TableCell addColumn(Object data) {
    TableCell cell = AbstractTable.convertCell(data);
    cells.add(cell);
    return cell;
  }

  /**
   * Do NOT use this. You should be using addColumn instead
   *
   * @return
   */
  public List<TableCell> getCells() {
    return cells;
  }

  public SelectionsTableSelection addClass(String extraClass) {
    if (!Check.isEmpty(extraClass)) {
      if (styleClasses == null) {
        styleClasses = new HashSet<String>();
      }
      styleClasses.add(extraClass);
    }
    return this;
  }

  @SuppressWarnings("nls")
  public SelectionsTableSelection addClasses(String styleClass) {
    String[] classes = styleClass.split("\\s+");
    for (String clazz : classes) {
      addClass(clazz);
    }
    return this;
  }

  public SelectionsTableSelection addClasses(Set<String> classes) {
    for (String clazz : classes) {
      addClass(clazz);
    }
    return this;
  }

  public boolean isClassesSet() {
    return !Check.isEmpty(styleClasses);
  }

  public Set<String> getStyleClasses() {
    return styleClasses;
  }
}
