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

package com.tle.web.api.search.bean;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Bean defining a search type within My Resources (e.g. Published, Drafts, Scrapbook). Contains
 * metadata about the search type such as its ID, display name, item count, and any sub-searches
 * (e.g. Moderation Queue statuses).
 */
public class MyResourceStatusSummaryBean {
  /** The display name of the search type. */
  private String name;

  /** The unique identifier of the search type. */
  private String id;

  /** The number of items matching this search type. */
  private int count;

  /**
   * The URI link to execute the search of particular search type. This field is hidden (null) for
   * sub-searches.
   */
  @JsonInclude(Include.NON_NULL)
  private URI links;

  /**
   * A list of nested search definitions, such as "In moderation" or "Rejected" within theModeration
   * Queue. This field is hidden if the list is empty.
   */
  @JsonInclude(Include.NON_EMPTY)
  private List<MyResourceStatusSummaryBean> subSearches = new ArrayList<>();

  /**
   * @param name The display name of the search type.
   * @param id The ID of the search type.
   * @param count The count of items matching this search type.
   */
  public MyResourceStatusSummaryBean(String name, String id, int count) {
    this.name = name;
    this.id = id;
    this.count = count;
  }

  /**
   * @param name The display name of the search type.
   * @param id The ID of the search type.
   * @param count The count of items matching this search type.
   * @param link The URI link to execute this search.
   */
  public MyResourceStatusSummaryBean(String name, String id, int count, URI link) {
    this.name = name;
    this.id = id;
    this.count = count;
    this.links = link;
  }

  /**
   * @return The ID of the search type.
   */
  public String getId() {
    return id;
  }

  /**
   * @param id The ID of the search type.
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * @return The display name of the search type.
   */
  public String getName() {
    return name;
  }

  /**
   * @param name The display name of the search type.
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * @return The count of items matching this search type.
   */
  public int getCount() {
    return count;
  }

  /**
   * @param count The count of items matching this search type.
   */
  public void setCount(int count) {
    this.count = count;
  }

  /**
   * Returns the URI link to execute the search of particular search type.
   *
   * @return The URI link, or null if this is a sub-search.
   */
  public URI getLinks() {
    return links;
  }

  /**
   * Sets the URI link for this search type.
   *
   * @param links The URI link to set.
   */
  public void setLinks(URI links) {
    this.links = links;
  }

  /**
   * @return A list of sub-searches (e.g. moderation queue statuses). Returns an empty list if there
   *     are none.
   */
  public List<MyResourceStatusSummaryBean> getSubSearches() {
    return subSearches;
  }

  /**
   * @param subSearches The list of sub-searches.
   */
  public void setSubSearches(List<MyResourceStatusSummaryBean> subSearches) {
    this.subSearches = subSearches;
  }
}
