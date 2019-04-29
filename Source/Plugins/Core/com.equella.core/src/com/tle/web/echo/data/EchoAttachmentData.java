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

package com.tle.web.echo.data;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import java.util.List;

public class EchoAttachmentData {
  @JsonUnwrapped private EchoData echoData = new EchoData();

  private List<EchoPresenter> presenters;

  public EchoAttachmentData() {
    // Nothing
  }

  public EchoAttachmentData(EchoData echoData, List<EchoPresenter> presenters) {
    this.echoData = echoData;
    this.presenters = presenters;
  }

  public List<EchoPresenter> getPresenters() {
    return presenters;
  }

  public void setPresenters(List<EchoPresenter> presenters) {
    this.presenters = presenters;
  }

  public EchoData getEchoData() {
    return echoData;
  }

  public void setEchoData(EchoData echoData) {
    this.echoData = echoData;
  }
}
