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

package com.tle.core.security.impl;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents security attributes for method invocations, including privileges required for
 * execution, the mode of security checks, and whether the method is system-only or requires
 * specific domain object context.
 *
 * <p>This class is used by the {@link MethodSecurityInterceptor} to determine how to apply security
 * checks based on the provided attributes.
 */
public class SecurityAttribute {
  /**
   * Specifies the mode of a security check for a method invocation, determining the context against
   * which a user's privileges are evaluated. This is used by the MethodSecurityInterceptor to
   * decide how to apply ACL checks for annotations like {@code SecureOnCall} and {@code
   * RequiresPrivilege}.
   *
   * @see com.tle.core.security.impl.MethodSecurityInterceptor#invoke
   */
  public enum OnCallMode {
    /**
     * Indicates the security check must be performed against a specific domain object (entity) that
     * is passed as an argument to the secured method. The interceptor will dynamically identify
     * this argument and check if the current user has the required privilege for that specific
     * object.
     */
    DOMAIN,

    /**
     * Indicates the security check should be performed without a specific domain object context,
     * implying a check against the top-level of the institution. The check verifies if the user has
     * the privilege granted institution-wide.
     */
    TOPLEVEL,

    /**
     * Indicates the security check only requires that the user possesses the specified privilege
     * anywhere in the system. The check is not scoped to a specific domain object and will pass if
     * the user has the privilege granted for any entity or at the institutional level.
     */
    ANY
  }

  private Set<String> onCallPrivileges;
  private Set<String> filterPrivileges;
  private OnCallMode onCallMode;
  private boolean systemOnly;
  private boolean filterMatching;
  private int domainArg;

  public boolean isSystemOnly() {
    return systemOnly;
  }

  public void setSystemOnly(boolean systemOnly) {
    this.systemOnly = systemOnly;
  }

  /**
   * Indicates whether the security check is for filtering results after method execution. If true,
   * the method will execute normally, but the results will be filtered based on the user's
   * privileges. This is typically used in conjunction with {@link #filterPrivileges} to restrict
   * the returned data.
   *
   * @return true if filtering is applied, false otherwise
   */
  public boolean isFilterMatching() {
    return filterMatching;
  }

  public void setFilterMatching(boolean filterMatching) {
    this.filterMatching = filterMatching;
  }

  /**
   * Gets the index of the argument in the method signature that represents the domain object for
   * which the security check is being performed. This is used when {@link #onCallMode} is set to
   * {@link OnCallMode#DOMAIN}.
   *
   * @return the index of the domain object argument
   */
  public int getDomainArg() {
    return domainArg;
  }

  /**
   * Sets the index of the argument in the method signature that represents the domain object for
   * which the security check is being performed. This is used when {@link #onCallMode} is set to
   * {@link OnCallMode#DOMAIN}.
   *
   * @param domainArg the index of the domain object argument
   */
  public void setDomainArg(int domainArg) {
    this.domainArg = domainArg;
  }

  public Set<String> getOnCallPrivileges() {
    return onCallPrivileges;
  }

  public Set<String> getFilterPrivileges() {
    return filterPrivileges;
  }

  public void addOnCallPrivilege(String priv) {
    if (onCallPrivileges == null) {
      onCallPrivileges = new HashSet<>();
    }
    onCallPrivileges.add(priv);
  }

  public void addFilterPrivilege(String priv) {
    if (filterPrivileges == null) {
      filterPrivileges = new HashSet<>();
    }
    filterPrivileges.add(priv);
  }

  /**
   * Gets the mode of the security check for a method invocation, determining how the user's
   * privileges are evaluated against the secured method. Primarily will it check against a specific
   * domain object (entity) passed as an argument, against the top-level of the institution, or
   * simply check if the user has the required privilege anywhere in the system.
   *
   * @return the mode of the security check
   */
  public OnCallMode getOnCallMode() {
    return onCallMode;
  }

  /**
   * Sets the mode of the security check for a method invocation, determining how the user's
   * privileges are evaluated against the secured method. Primarily will it check against a specific
   * domain object (entity) passed as an argument, against the top-level of the institution, or
   * simply check if the user has the required privilege anywhere in the system.
   *
   * @param onCallMode the mode of the security check
   */
  public void setOnCallMode(OnCallMode onCallMode) {
    this.onCallMode = onCallMode;
  }
}
