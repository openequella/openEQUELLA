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

package com.tle.beans.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import org.hibernate.annotations.AccessType;

@Entity
@AccessType("field")
public class FederatedSearch extends BaseEntity {
  private static final long serialVersionUID = 1L;

  @Column(length = 64)
  private String type;

  private int timeout;

  private String collectionUuid;

  @Lob private String advancedSearchFields;

  public String getAdvancedSearchFields() {
    return advancedSearchFields;
  }

  public void setAdvancedSearchFields(String advancedSearchFields) {
    this.advancedSearchFields = advancedSearchFields;
  }

  public FederatedSearch() {
    timeout = 30;
  }

  public int getTimeout() {
    return timeout;
  }

  public void setTimeout(int timeout) {
    this.timeout = timeout;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getCollectionUuid() {
    return collectionUuid;
  }

  public void setCollectionUuid(String collectionUuid) {
    this.collectionUuid = collectionUuid;
  }
}
