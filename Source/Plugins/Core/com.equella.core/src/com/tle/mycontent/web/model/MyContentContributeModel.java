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

package com.tle.mycontent.web.model;

import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.equella.layout.TwoColumnLayout;
import com.tle.web.sections.generic.CachedData;
import java.util.Set;

public class MyContentContributeModel extends TwoColumnLayout.TwoColumnModel {
  @Bookmarked private String contributeId;
  private final CachedData<Set<String>> allowedHandlers = new CachedData<Set<String>>();

  public String getContributeId() {
    return contributeId;
  }

  public void setContributeId(String contributeId) {
    this.contributeId = contributeId;
  }

  public CachedData<Set<String>> getAllowedHandlers() {
    return allowedHandlers;
  }

  /**
   * This field must be bookmarked so its value can be kept when Sections are forwarding to each
   * other. Also, this one is only used when the request to create or edit a Scrapbook comes from
   * New UI directly.
   */
  @Bookmarked private String newUIStateId;

  public String getNewUIStateId() {
    return newUIStateId;
  }

  public void setNewUIStateId(String newUIStateId) {
    this.newUIStateId = newUIStateId;
  }
}
