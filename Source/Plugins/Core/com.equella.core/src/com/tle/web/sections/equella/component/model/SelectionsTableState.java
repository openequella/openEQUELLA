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
import com.tle.web.sections.render.Label;
import com.tle.web.sections.standard.AbstractEventOnlyComponent;
import com.tle.web.sections.standard.model.TableState;
import java.util.List;

/**
 * The class formerly known as CurrentlySelectedStuff
 *
 * @author aholland
 */
public class SelectionsTableState extends TableState {
  private Label nothingSelectedText;
  private AbstractEventOnlyComponent<?> addAction;
  private List<SelectionsTableSelection> selections;

  @SuppressWarnings("nls")
  public SelectionsTableState() {
    super("selectedstuff");
  }

  public List<SelectionsTableSelection> getSelections() {
    if (selections == null) {
      selections = Lists.newArrayList();
    }
    return selections;
  }

  public void setSelections(List<SelectionsTableSelection> selections) {
    this.selections = selections;
  }

  public Label getNothingSelectedText() {
    return nothingSelectedText;
  }

  public void setNothingSelectedText(Label nothingSelectedText) {
    this.nothingSelectedText = nothingSelectedText;
  }

  public AbstractEventOnlyComponent<?> getAddAction() {
    return addAction;
  }

  public void setAddAction(AbstractEventOnlyComponent<?> addAction) {
    this.addAction = addAction;
  }
}
