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

import com.tle.web.sections.standard.RendererConstants;
import com.tle.web.sections.standard.renderers.list.DropDownRenderer;
import java.util.List;

/**
 * The State class for mutable lists.
 *
 * <p>The difference between a {@code HtmlMutableListState} and {@link HtmlListState}, is that the
 * mutable list is designed for modifying the values of the list, where as the {@code HtmlListState}
 * is about selecting values from the pre-defined list.
 *
 * @see HtmlListState
 * @see DropDownRenderer
 * @author jmaginnis
 */
public class HtmlMutableListState extends HtmlComponentState {
  private List<Option<?>> options;
  private boolean grouped;

  public HtmlMutableListState() {
    super(RendererConstants.DROPDOWN);
  }

  public List<Option<?>> getOptions() {
    return options;
  }

  public void setOptions(List<Option<?>> options) {
    this.options = options;
  }

  public boolean isGrouped() {
    return grouped;
  }

  public void setGrouped(boolean grouped) {
    this.grouped = grouped;
  }
}
