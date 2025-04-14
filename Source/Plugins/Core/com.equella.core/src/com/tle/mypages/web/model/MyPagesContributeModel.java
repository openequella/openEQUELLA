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

package com.tle.mypages.web.model;

import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.render.SectionRenderable;
import java.util.List;

public class MyPagesContributeModel {
  @Bookmarked(name = "m")
  private boolean modal;

  @Bookmarked(name = "s")
  private String session;

  @Bookmarked(name = "p")
  private String pageUuid;

  @Bookmarked(name = "i")
  private String itemId;

  @Bookmarked(name = "c")
  private String finishedCallback;

  // used just once upon entry, then reset
  @Bookmarked(name = "l")
  private boolean load;

  private boolean showPreviewCheckBox;

  public boolean isShowPreviewCheckBox() {
    return showPreviewCheckBox;
  }

  public void setShowPreviewCheckBox(boolean showPreviewCheckBox) {
    this.showPreviewCheckBox = showPreviewCheckBox;
  }

  private List<SectionRenderable> renderables;

  public boolean isModal() {
    return modal;
  }

  public void setModal(boolean modal) {
    this.modal = modal;
  }

  public String getSession() {
    return session;
  }

  public void setSession(String session) {
    this.session = session;
  }

  public String getPageUuid() {
    return pageUuid;
  }

  public void setPageUuid(String pageUuid) {
    this.pageUuid = pageUuid;
  }

  public String getItemId() {
    return itemId;
  }

  public void setItemId(String itemId) {
    this.itemId = itemId;
  }

  public boolean isLoad() {
    return load;
  }

  public void setLoad(boolean load) {
    this.load = load;
  }

  public String getFinishedCallback() {
    return finishedCallback;
  }

  public void setFinishedCallback(String finishedCallback) {
    this.finishedCallback = finishedCallback;
  }

  public List<SectionRenderable> getRenderables() {
    return renderables;
  }

  public void setRenderables(List<SectionRenderable> renderables) {
    this.renderables = renderables;
  }
}
