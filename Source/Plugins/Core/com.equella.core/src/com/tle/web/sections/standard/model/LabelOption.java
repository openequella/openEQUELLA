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

package com.tle.web.sections.standard.model;

import com.tle.web.sections.render.Label;

public class LabelOption<T> implements Option<T> {
  private final Label label;
  private final String value;
  private final T object;
  private boolean disabled;

  public LabelOption(Label label, String value, T object) {
    this.label = label;
    this.value = value;
    this.object = object;
  }

  @Override
  public String getName() {
    return label.getText();
  }

  @Override
  public boolean isNameHtml() {
    return label.isHtml();
  }

  @Override
  public T getObject() {
    return object;
  }

  @Override
  public String getValue() {
    return value;
  }

  @Override
  public boolean isDisabled() {
    return disabled;
  }

  @Override
  public void setDisabled(boolean disabled) {
    this.disabled = disabled;
  }

  @Override
  public boolean hasAltTitleAttr() {
    return false;
  }

  @Override
  public String getAltTitleAttr() {
    return null;
  }

  @Override
  public String getGroupName() {
    return null;
  }
}
