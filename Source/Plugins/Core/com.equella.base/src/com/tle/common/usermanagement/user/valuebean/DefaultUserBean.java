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

package com.tle.common.usermanagement.user.valuebean;

import com.tle.common.Check;
import com.tle.common.Check.FieldEquality;
import com.tle.common.Format;
import java.util.Objects;

public class DefaultUserBean implements UserBean, FieldEquality<DefaultUserBean> {
  private static final long serialVersionUID = 1L;
  private final String id;
  private final String username;
  private final String firstName;
  private final String lastName;
  private final String emailAddress;

  public DefaultUserBean(
      String id, String username, String firstName, String lastName, String emailAddress) {
    Check.checkNotNull(id);
    Check.checkNotNull(username);
    Check.checkNotNull(firstName);
    Check.checkNotNull(lastName);

    this.id = id;
    this.username = username;
    this.firstName = firstName;
    this.lastName = lastName;
    this.emailAddress = emailAddress;
  }

  @Override
  public String getUniqueID() {
    return id;
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
  public int hashCode() {
    return id.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    return Check.commonEquals(this, obj);
  }

  @Override
  public boolean checkFields(DefaultUserBean rhs) {
    return Objects.equals(id, rhs.id);
  }

  @Override
  public String toString() {
    return Format.format(this);
  }
}
