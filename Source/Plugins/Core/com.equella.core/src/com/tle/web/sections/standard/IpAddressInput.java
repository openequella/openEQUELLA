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

package com.tle.web.sections.standard;

import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.events.DocumentParamsEvent;
import com.tle.web.sections.standard.js.JSValueComponent;
import com.tle.web.sections.standard.model.HtmlIpAddressInputState;

public class IpAddressInput
    extends AbstractValueStateComponent<HtmlIpAddressInputState, JSValueComponent> {

  public IpAddressInput() {
    super(RendererConstants.IP);
  }

  @Override
  public Object instantiateModel(SectionInfo info) {
    return setupState(info, new HtmlIpAddressInputState());
  }

  public String getValue(SectionInfo info) {
    return getState(info).getValue();
  }

  public void setValue(SectionInfo info, String value) {
    getState(info).setValue(value);
  }

  @Override
  protected String getBookmarkStringValue(HtmlIpAddressInputState state) {
    return state.getValue();
  }

  @Override
  public void document(SectionInfo info, DocumentParamsEvent event) {
    // nah mate
  }
}
