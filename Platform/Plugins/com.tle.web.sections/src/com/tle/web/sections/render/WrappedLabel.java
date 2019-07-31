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

package com.tle.web.sections.render;

public class WrappedLabel implements ProcessedLabel {
  private final Label label;
  private int maxBodyLength;
  private boolean inline;
  private boolean showAltText;

  private String truncText;

  public WrappedLabel(Label label, int maxBodyLength) {
    this(label, maxBodyLength, false, true);
  }

  public WrappedLabel(Label label, int maxBodyLength, boolean showAltText) {
    this(label, maxBodyLength, showAltText, true);
  }

  public WrappedLabel(Label label, int maxBodyLength, boolean showAltText, boolean inline) {
    this.label = label;
    this.maxBodyLength = maxBodyLength;
    this.showAltText = showAltText;
    this.inline = inline;
  }

  public WrappedLabel setMaxBodyLength(int maxBodyLength) {
    this.maxBodyLength = maxBodyLength;
    return this;
  }

  public int getMaxBodyLength() {
    return maxBodyLength;
  }

  public WrappedLabel setShowAltText(boolean showAltText) {
    this.showAltText = showAltText;
    return this;
  }

  public boolean isShowAltText() {
    return showAltText;
  }

  public WrappedLabel setInline(boolean inline) {
    this.inline = inline;
    return this;
  }

  public boolean isInline() {
    return inline;
  }

  private void build() {
    String fullText = label.getText();
    if (maxBodyLength < 0 || fullText.length() < maxBodyLength) {
      truncText = fullText;
      return;
    }
    truncText = TextUtils.INSTANCE.ensureWrap(fullText, maxBodyLength, -1, label.isHtml());
  }

  @Override
  public String getText() {
    if (truncText == null) {
      build();
    }
    return truncText;
  }

  @Override
  public Label getUnprocessedLabel() {
    return label;
  }

  @Override
  public boolean isHtml() {
    return label.isHtml();
  }
}
