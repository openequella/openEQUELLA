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

package com.tle.core.connectors.blackboard.beans;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Availability implements Serializable {
  public static final String YES = "Yes";
  public static final String NO = "No";
  private String available; // Yes
  private Boolean allowGuests;

  // private Object adaptiveRelease;

  public String getAvailable() {
    return available;
  }

  public void setAvailable(String available) {
    this.available = available;
  }

  public Boolean getAllowGuests() {
    return allowGuests;
  }

  public void setAllowGuests(Boolean allowGuests) {
    this.allowGuests = allowGuests;
  }

  @XmlRootElement
  public static class Duration {
    private String type; // Continuous

    public String getType() {
      return type;
    }

    public void setType(String type) {
      this.type = type;
    }
  }

  public String toString() {
    return "available=" + available;
  }
}
