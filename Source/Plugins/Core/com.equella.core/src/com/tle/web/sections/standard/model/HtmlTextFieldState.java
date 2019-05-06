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

import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.standard.RendererConstants;

public class HtmlTextFieldState extends HtmlValueState {
  private boolean password;
  private int size;
  private int maxLength;
  private boolean editable = true;
  private boolean autocompleteDisabled;
  private JSCallable callback;

  public HtmlTextFieldState() {
    super(RendererConstants.TEXTFIELD);
  }

  public boolean isPassword() {
    return password;
  }

  public void setPassword(boolean password) {
    this.password = password;
  }

  public int getSize() {
    return size;
  }

  public void setSize(int size) {
    this.size = size;
  }

  public int getMaxLength() {
    return maxLength;
  }

  public void setMaxLength(int maxLength) {
    this.maxLength = maxLength;
  }

  public boolean isAutocompleteDisabled() {
    return autocompleteDisabled;
  }

  public void setAutocompleteDisabled(boolean autocompleteDisabled) {
    this.autocompleteDisabled = autocompleteDisabled;
  }

  public boolean isEditable() {
    return editable;
  }

  public void setEditable(boolean editable) {
    this.editable = editable;
  }

  public JSCallable getAutoCompleteCallback() {
    return callback;
  }

  public void setAutoCompleteCallback(JSCallable callback) {
    this.callback = callback;
  }
}
