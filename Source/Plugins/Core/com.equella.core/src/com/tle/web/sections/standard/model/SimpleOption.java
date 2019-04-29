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

public class SimpleOption<T> implements Option<T> {
  private String name;
  private String value;
  private boolean disabled;
  private T object;

  public SimpleOption(String name, String value) {
    this(name, value, null, false);
  }

  public SimpleOption(String name, String value, T object) {
    this(name, value, object, false);
  }

  public SimpleOption(String name, String value, T object, boolean disabled) {
    this.name = name;
    this.value = value;
    this.object = object;
    this.disabled = disabled;
  }

  @Override
  public T getObject() {
    return object;
  }

  @Override
  public void setDisabled(boolean disabled) {
    this.disabled = disabled;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public boolean isNameHtml() {
    return false;
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
  public boolean hasAltTitleAttr() {
    return false;
  }

  @Override
  public String getAltTitleAttr() {
    return null;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }

    if (!(obj instanceof Option<?>)) {
      return false;
    }

    return ((Option<?>) obj).getValue().equals(value);
  }

  @Override
  public int hashCode() {
    return value.hashCode();
  }

  @Override
  public String getGroupName() {
    return null;
  }
}
