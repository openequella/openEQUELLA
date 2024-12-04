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

package com.tle.beans.hierarchy;

import com.thoughtworks.xstream.annotations.XStreamOmitField;
import com.tle.beans.Institution;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import org.hibernate.annotations.AttributeAccessor;
import org.hibernate.annotations.Index;

@Entity
@Table(name = "hierarchy_topic_key_resources")
@AttributeAccessor("field")
@NamedQuery(
    name = "getByItemUuidAndInstitution",
    query =
        "FROM HierarchyTopicKeyResource t WHERE t.itemUuid = :itemUuid AND t.institution ="
            + " :institution")
public class HierarchyTopicKeyResource {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private long id;

  @JoinColumn(nullable = false)
  @ManyToOne(fetch = FetchType.LAZY)
  @Index(name = "key_resource_institution")
  @XStreamOmitField
  private Institution institution;

  @Index(name = "key_resource_hierarchy_uuid")
  @Column(length = 1024)
  private String hierarchyCompoundUuid;

  @Index(name = "key_resource_item_uuid")
  @Column(length = 40)
  private String itemUuid;

  @Index(name = "key_resource_item_version")
  private int itemVersion;

  @Column(nullable = false)
  private Date dateCreated;

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public Institution getInstitution() {
    return institution;
  }

  public void setInstitution(Institution institution) {
    this.institution = institution;
  }

  public String getHierarchyCompoundUuid() {
    return hierarchyCompoundUuid;
  }

  public void setHierarchyCompoundUuid(String dynamicHierarchyId) {
    this.hierarchyCompoundUuid = dynamicHierarchyId;
  }

  public String getItemUuid() {
    return itemUuid;
  }

  public void setItemUuid(String uuid) {
    this.itemUuid = uuid;
  }

  public int getItemVersion() {
    return itemVersion;
  }

  public void setItemVersion(int version) {
    this.itemVersion = version;
  }

  public Date getDateCreated() {
    return dateCreated;
  }

  public void setDateCreated(Date dateCreated) {
    this.dateCreated = dateCreated;
  }
}
