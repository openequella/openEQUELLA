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

package com.tle.web.sections.equella.utils;

import com.tle.web.sections.SectionId;

public class SettingControl {
  private String label;
  private SectionId component;
  private String error;
  private String help;
  private boolean mandatory;

  public SettingControl(
      String label, SectionId component, String error, String help, boolean mandatory) {
    this.label = label;
    this.component = component;
    this.error = error;
    this.help = help;
    this.mandatory = mandatory;
  }

  public SettingControl(String label, SectionId component) {
    this(label, component, null, null, false);
  }

  public SettingControl(String label, SectionId component, boolean mandatory) {
    this(label, component, null, null, mandatory);
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public SectionId getComponent() {
    return component;
  }

  public void setComponent(SectionId component) {
    this.component = component;
  }

  public String getError() {
    return error;
  }

  public void setError(String errro) {
    this.error = errro;
  }

  public String getHelp() {
    return help;
  }

  public void setHelp(String help) {
    this.help = help;
  }

  public boolean isMandatory() {
    return mandatory;
  }

  public void setMandatory(boolean mandatory) {
    this.mandatory = mandatory;
  }
}
