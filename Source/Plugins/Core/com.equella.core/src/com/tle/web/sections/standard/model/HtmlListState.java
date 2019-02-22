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

package com.tle.web.sections.standard.model;

import com.google.common.collect.Sets;
import com.tle.web.sections.standard.MultiSelectionList;
import com.tle.web.sections.standard.SingleSelectionList;
import java.util.Set;

/**
 * The State class for selectable lists.
 *
 * <p>Allows for single/multiple selection and keeps a set of selected values. Thus everything in
 * the list must be uniquely identifiable.
 *
 * @see SingleSelectionList
 * @see MultiSelectionList
 * @author jmaginnis
 */
public class HtmlListState extends HtmlMutableListState {
  private boolean disallowMultiple;
  private boolean multiple;
  private Set<String> selectedValues;

  public boolean isDisallowMultiple() {
    return disallowMultiple;
  }

  public void setDisallowMultiple(boolean disallowMultiple) {
    this.disallowMultiple = disallowMultiple;
  }

  public boolean isMultiple() {
    return multiple;
  }

  public void setMultiple(boolean multiple) {
    this.multiple = multiple;
  }

  public Set<String> getSelectedValues() {
    return selectedValues;
  }

  public void setSelectedValues(Set<String> selectedValues) {
    this.selectedValues = selectedValues;
  }

  public void setSelectedValues(String... selectedValues) {
    this.selectedValues = Sets.newHashSet(selectedValues);
  }
}
