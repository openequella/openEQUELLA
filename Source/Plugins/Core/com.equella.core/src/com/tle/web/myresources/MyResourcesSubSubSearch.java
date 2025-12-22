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

package com.tle.web.myresources;

import com.tle.common.search.DefaultSearch;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.render.Label;

/**
 * Represents a sub-category of search within My Resources search types. (e.g. specific moderation
 * statuses within the 'Moderation Queue' type).
 */
public abstract class MyResourcesSubSubSearch {
  private final String id;
  private final Label name;
  private final DefaultSearch search;

  /**
   * @param id The unique identifier for this sub-search.
   * @param name The display label for this sub-search.
   * @param search The search criteria definition.
   */
  public MyResourcesSubSubSearch(String id, Label name, DefaultSearch search) {
    this.id = id;
    this.name = name;
    this.search = search;
  }

  /**
   * Execute logic associated with this sub-search given the current section info.
   *
   * @param info The current SectionInfo.
   */
  public abstract void execute(SectionInfo info);

  /**
   * @return The unique identifier for this sub-search.
   */
  public String getId() {
    return id;
  }

  /**
   * @return The display label for this sub-search.
   */
  public Label getName() {
    return name;
  }

  /**
   * @return The search criteria definition.
   */
  public DefaultSearch getSearch() {
    return search;
  }
}
