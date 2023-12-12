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

package com.tle.common.oauth.beans;

import com.tle.beans.entity.BaseEntity;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Index;

@Entity
@AccessType("field")
// TODO: how do you do this when it's across two tables????
// @Table(uniqueConstraints = {@UniqueConstraint(columnNames =
// {"institution_id", "clientId"})})
public final class OAuthClient extends BaseEntity {
  private static final long serialVersionUID = 1L;

  @Index(name = "oauthClientIdIndex")
  @Column(nullable = false, length = 100)
  private String clientId;

  @Lob private String clientSecret;

  @Index(name = "oauthRedirectURLIndex")
  @Column(nullable = false, length = 1024)
  private String redirectUrl;

  /**
   * The set of permissions that the OAuthClient is allowed to <em>request</em>. Currently, apps can
   * do anything
   */
  @ElementCollection(fetch = FetchType.EAGER)
  private Set<String> permissions;

  /** Client can optionally login as a certain fixed user */
  @Column(length = 255)
  private String userId;

  /** Currently, no clients require user approval */
  @Column(nullable = false)
  private boolean requiresApproval;

  /**
   * The number of days tokens generated for this client are valid for. 0 means they never expire.
   */
  @Column(nullable = false)
  private int tokenValidity;

  public String getClientId() {
    return clientId;
  }

  public void setClientId(String clientId) {
    this.clientId = clientId;
  }

  public String getClientSecret() {
    return clientSecret;
  }

  public void setClientSecret(String clientSecret) {
    this.clientSecret = clientSecret;
  }

  public String getRedirectUrl() {
    return redirectUrl;
  }

  public void setRedirectUrl(String redirectUrl) {
    this.redirectUrl = redirectUrl;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public Set<String> getPermissions() {
    return permissions;
  }

  public void setPermissions(Set<String> permissions) {
    this.permissions = permissions;
  }

  public boolean isRequiresApproval() {
    return requiresApproval;
  }

  public void setRequiresApproval(boolean requiresApproval) {
    this.requiresApproval = requiresApproval;
  }

  /**
   * Returns the number of days for which tokens generated for this client are valid. 0 means they
   * never expire.
   */
  public int getTokenValidity() {
    return tokenValidity;
  }

  /**
   * Where tokenValidity is 0, tokens never expire, and all other values should be positive.
   *
   * @param tokenValidity number of days tokens generated for this client are valid.
   */
  public void setTokenValidity(int tokenValidity) {
    if (tokenValidity < 0) {
      throw new IllegalArgumentException("Token validity must be >= 0");
    }

    this.tokenValidity = tokenValidity;
  }
}
