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

package com.tle.beans.audit;

import com.thoughtworks.xstream.annotations.XStreamOmitField;
import com.tle.beans.Institution;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import org.hibernate.annotations.AttributeAccessor;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.Type;

@Entity
@AttributeAccessor("field")
public class AuditLogEntry implements AuditLogTable {
  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @Index(name = "audit_institution_id")
  @XStreamOmitField
  private Institution institution;

  @Index(name = "audit_timestamp")
  private Date timestamp;

  @Column(length = 20)
  @Index(name = "audit_event_category")
  private String eventCategory;

  @Column(length = 20)
  @Index(name = "audit_event_type")
  private String eventType;

  @Column(length = 255, nullable = false)
  @Index(name = "audit_user_id")
  private String userId;

  @Column(length = 40)
  @Index(name = "audit_session_id")
  private String sessionId;

  @Column(length = 255)
  @Index(name = "audit_data1")
  private String data1;

  @Column(length = 255)
  @Index(name = "audit_data2")
  private String data2;

  @Column(length = 255)
  @Index(name = "audit_data3")
  private String data3;

  @Lob private String data4;

  @Type(type = "json")
  private String meta;

  public AuditLogEntry() {
    super();
  }

  public AuditLogEntry(
      String userId,
      String sessionId,
      String category,
      String type,
      Date timestamp,
      String d1,
      String d2,
      String d3,
      String d4,
      Institution institution,
      String meta) {
    this.userId = userId;
    this.sessionId = sessionId;
    this.eventCategory = category;
    this.eventType = type;
    this.data1 = d1;
    this.data2 = d2;
    this.data3 = d3;
    this.data4 = d4;
    this.institution = institution;
    this.timestamp = timestamp;
    this.meta = meta;
  }

  @Override
  public long getId() {
    return id;
  }

  @Override
  public void setId(long id) {
    this.id = id;
  }

  @Override
  public Institution getInstitution() {
    return institution;
  }

  @Override
  public void setInstitution(Institution institution) {
    this.institution = institution;
  }

  @Override
  public Date getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Date timestamp) {
    this.timestamp = timestamp;
  }

  public String getEventCategory() {
    return eventCategory;
  }

  public void setEventCategory(String eventCategory) {
    this.eventCategory = eventCategory;
  }

  public String getEventType() {
    return eventType;
  }

  public void setEventType(String eventType) {
    this.eventType = eventType;
  }

  @Override
  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  @Override
  public String getSessionId() {
    return sessionId;
  }

  public void setSessionId(String sessionId) {
    this.sessionId = sessionId;
  }

  public String getData1() {
    return data1;
  }

  public void setData1(String data1) {
    this.data1 = data1;
  }

  public String getData2() {
    return data2;
  }

  public void setData2(String data2) {
    this.data2 = data2;
  }

  public String getData3() {
    return data3;
  }

  public void setData3(String data3) {
    this.data3 = data3;
  }

  public String getData4() {
    return data4;
  }

  public void setData4(String data4) {
    this.data4 = data4;
  }

  public String getMeta() {
    return meta;
  }

  public void setMeta(String meta) {
    this.meta = meta;
  }
}
