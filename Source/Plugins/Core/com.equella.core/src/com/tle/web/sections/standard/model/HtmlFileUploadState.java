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

import com.tle.web.sections.Bookmark;
import com.tle.web.sections.js.JSAssignable;

public class HtmlFileUploadState extends HtmlComponentState {
  private Bookmark ajaxUploadUrl;
  private JSAssignable validateFile;
  private boolean dontInitialise;

  public Bookmark getAjaxUploadUrl() {
    return ajaxUploadUrl;
  }

  public void setAjaxUploadUrl(Bookmark ajaxUploadUrl) {
    this.ajaxUploadUrl = ajaxUploadUrl;
  }

  public JSAssignable getValidateFile() {
    return validateFile;
  }

  public void setValidateFile(JSAssignable validateFile) {
    this.validateFile = validateFile;
  }

  public void setDontInitialise(boolean dontInitialise) {
    this.dontInitialise = dontInitialise;
  }

  public boolean isDontInitialise() {
    return dontInitialise;
  }
}
