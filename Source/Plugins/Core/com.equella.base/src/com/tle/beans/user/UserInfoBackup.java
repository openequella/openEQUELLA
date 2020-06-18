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

package com.tle.beans.user;

import com.tle.common.usermanagement.user.valuebean.UserBean;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import org.hibernate.annotations.AccessType;

/**
 * The background of adding this class is the feature of 'last known user'. This class maps the
 * table 'user_info_backup' which stores details of users who at least have login once, such as
 * their unique IDs, given names and surnames.
 */
@Entity
@AccessType("field")
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"uniqueId", "institutionId"})})
public class UserInfoBackup implements UserBean {
  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private long id;

  @Column(nullable = false)
  private String username;

  @Column private String firstName;

  @Column private String lastName;

  @Column private String emailAddress;

  @Column(nullable = false)
  private long institutionId;

  @Column(nullable = false)
  private String uniqueId;

  public void setUsername(String username) {
    this.username = username;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public void setEmailAddress(String emailAddress) {
    this.emailAddress = emailAddress;
  }

  public void setInstitution_id(long institutionId) {
    this.institutionId = institutionId;
  }

  public void setUniqueId(String uniqueId) {
    this.uniqueId = uniqueId;
  }

  @Override
  public String getUsername() {
    return username;
  }

  @Override
  public String getFirstName() {
    return firstName;
  }

  @Override
  public String getLastName() {
    return lastName;
  }

  @Override
  public String getEmailAddress() {
    return emailAddress;
  }

  @Override
  public String getUniqueID() {
    return uniqueId;
  }
}
