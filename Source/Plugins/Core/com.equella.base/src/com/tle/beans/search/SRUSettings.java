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

package com.tle.beans.search;

public class SRUSettings extends XmlBasedSearchSettings {
  private static final String SEARCH_TYPE = "SRUSearchEngine";

  private String url;
  private String schemaId;

  /**
   * @see com.tle.beans.search.SearchSettings#getType()
   */
  @Override
  protected String getType() {
    return SEARCH_TYPE;
  }

  @Override
  protected void _load() {
    super._load();
    url = get("url", url);
    schemaId = get("schema", schemaId);
  }

  @Override
  protected void _save() {
    super._save();
    put("url", url);
    put("schema", schemaId);
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getSchemaId() {
    return schemaId;
  }

  public void setSchemaId(String schemaId) {
    this.schemaId = schemaId;
  }
}
