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

import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.standard.model.HtmlComponentState;

public class BoxState extends HtmlComponentState {
  private boolean minimised;
  private JSCallable toggleMinimise;
  private boolean noMinMaxOnHeader;

  public boolean isMinimised() {
    return minimised;
  }

  public void setMinimised(boolean minimised) {
    this.minimised = minimised;
  }

  public JSCallable getToggleMinimise() {
    return toggleMinimise;
  }

  public void setToggleMinimise(JSCallable toggleMinimise) {
    this.toggleMinimise = toggleMinimise;
  }

  public boolean isNoMinMaxOnHeader() {
    return noMinMaxOnHeader;
  }

  public void setNoMinMaxOnHeader(boolean noMinMaxOnHeader) {
    this.noMinMaxOnHeader = noMinMaxOnHeader;
  }
}
