/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

public class SecurityAttribute {
  enum OnCallMode {
    DOMAIN,
    TOPLEVEL,
    ANY
  }

  private Set<String> onCallPrivileges;
  private Set<String> filterPrivileges;
  private OnCallMode onCallmode;
  private boolean systemOnly;
  private boolean filterMatching;
  private int domainArg;

  public boolean isSystemOnly() {
    return systemOnly;
  }

  public void setSystemOnly(boolean systemOnly) {
    this.systemOnly = systemOnly;
  }

  public boolean isFilterMatching() {
    return filterMatching;
  }

  public void setFilterMatching(boolean filterMatching) {
    this.filterMatching = filterMatching;
  }

  public int getDomainArg() {
    return domainArg;
  }

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
      onCallPrivileges = new HashSet<String>();
    }
    onCallPrivileges.add(priv);
  }

  public void addFilterPrivilege(String priv) {
    if (filterPrivileges == null) {
      filterPrivileges = new HashSet<String>();
    }
    filterPrivileges.add(priv);
  }

  public OnCallMode getOnCallmode() {
    return onCallmode;
  }

  public void setOnCallmode(OnCallMode onCallmode) {
    this.onCallmode = onCallmode;
  }
}
